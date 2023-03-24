package com.kidsability.automation.service;

import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.kidsability.automation.context.SharePointContext;
import com.kidsability.automation.context.secret.AzureCredentials;
import com.kidsability.automation.model.Client;
import com.kidsability.automation.util.GraphApiUtil;
import com.microsoft.graph.core.GraphErrorCodes;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.*;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class SharePointService {
    private GraphServiceClient graphServiceClient;
    private SharePointContext sharePointContext;
    public SharePointService(SharePointContext sharePointContext, AzureCredentials azureCredentials) {
        this.graphServiceClient = GraphApiUtil.getGraphClient(azureCredentials);
        this.sharePointContext = sharePointContext;
    }
    public DriveItem getDriveItemByPath(String path) throws RuntimeException {
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/root:/" + path;
        try {
            DriveItem driveItem = (DriveItem) graphServiceClient
                    .customRequest(url,DriveItem.class)
                    .buildRequest()
                    .get();
            return driveItem;
        }
        catch (GraphServiceException graphServiceException) {
            if(graphServiceException.getServiceError().isError(GraphErrorCodes.ITEM_NOT_FOUND)) return null;
            else throw new RuntimeException("Something went wrong");
        }
    }

    public DriveItem getDriveItemById(String id) throws RuntimeException {
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/" + id;
        try {
            DriveItem driveItem = (DriveItem) graphServiceClient
                    .customRequest(url,DriveItem.class)
                    .buildRequest()
                    .get();
            return driveItem;
        }
        catch (GraphServiceException graphServiceException) {
            if(graphServiceException.getServiceError().isError(GraphErrorCodes.ITEM_NOT_FOUND)) return null;
            else throw new RuntimeException("Something went wrong");
        }
    }

    public CompletableFuture<DriveItem> getDriveItemByIdFuture(String id) throws RuntimeException {
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/" + id;
        try {
            CompletableFuture<DriveItem> driveItemCompletableFuture = (CompletableFuture<DriveItem>) graphServiceClient
                    .customRequest(url,DriveItem.class)
                    .buildRequest()
                    .getAsync();
            return driveItemCompletableFuture;
        }
        catch (GraphServiceException graphServiceException) {
            if(graphServiceException.getServiceError().isError(GraphErrorCodes.ITEM_NOT_FOUND)) return null;
            else throw new RuntimeException("Something went wrong");
        }
    }



    public String getBase64Img(DriveItem driveItem) throws IOException {
        LinkedList<Option> requestOptions = new LinkedList<>();
        requestOptions.add(new QueryOption("format", "pdf"));

        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/" + driveItem.id + "/content";
        InputStream inputStream = (InputStream) graphServiceClient
                .customRequest(url, InputStream.class)
                .buildRequest(requestOptions)
                .get();
        // converts pdf to jpeg then base64 encodes it
        PDDocument document = PDDocument.load(inputStream);
        inputStream.close();
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        BufferedImage image = pdfRenderer.renderImage(0);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", byteArrayOutputStream);
        return Base64.getEncoder()
                        .encodeToString(byteArrayOutputStream.toByteArray());

    }

    public DriveItem createClientFolders(Client client) {
        var parentPath = "/General/Clients";
        DriveItem parent = getDriveItemByPath(parentPath);
        try {
            DriveItem clientRoot = createSubFolder(parent, "Client " + client.getKidsAbilityId())
                    .get();

            List<CompletableFuture<DriveItem>> futures = new ArrayList<>();
            futures.add(createSubFolder(clientRoot, "Programs"));
            futures.add(createSubFolder(clientRoot, "Mand Data"));
            futures.add(createSubFolder(clientRoot, "Behavioural Data"));
            futures.forEach((future) -> {
                try {
                    future.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            return clientRoot;
        }
        catch (Exception exception) {
            throw new RuntimeException();
        }
    }

    public CompletableFuture<DriveItem> createSubFolder(DriveItem parent, String childName) throws Exception{
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/" + parent.id + "/children";
        var child = new DriveItem();
        child.folder = new Folder();
        child.name = childName;
        return graphServiceClient.customRequest(url, DriveItem.class)
                .buildRequest()
                .postAsync(child);
    }

    public CompletableFuture<ItemPreviewInfo> getEmbeddableLinkFuture(DriveItem driveItem) {
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/" + driveItem.id + "/preview";
        return graphServiceClient.customRequest(url, ItemPreviewInfo.class)
                .buildRequest()
                .postAsync(new JsonObject());
    }

    public List<DriveItem> getChildren(DriveItem driveItem) {
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/" + driveItem.id + "/children";
        LinkedTreeMap res = (LinkedTreeMap) graphServiceClient.customRequest(url, LinkedTreeMap.class)
                .buildRequest()
                .get();
        var children = (ArrayList<LinkedTreeMap>) res.get("value");
        List<String> childrenIds = new ArrayList<>();
        for(var child : children) {
            childrenIds.add((String)child.get("id"));
        }
        List<CompletableFuture<DriveItem>> futures = new ArrayList<>();
        childrenIds.forEach(cId -> futures.add(getDriveItemByIdFuture(cId)));
        return futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    public DriveItem copyItem(DriveItem toCopy, DriveItem destParent, String copiedFileName) {
        ItemReference parentReference = new ItemReference();
        parentReference.siteId = sharePointContext.getSiteId();
        parentReference.id = destParent.id;

       DriveItem res =  graphServiceClient.sites(sharePointContext.getSiteId())
               .drive()
               .items(toCopy.id)
                .copy(DriveItemCopyParameterSet
                        .newBuilder()
                        .withName(copiedFileName)
                        .withParentReference(parentReference)
                        .build())
                .buildRequest()
                .post();
       return res;
    }

}
