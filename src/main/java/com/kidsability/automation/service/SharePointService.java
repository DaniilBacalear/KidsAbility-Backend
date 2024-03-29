package com.kidsability.automation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LinkedTreeMap;
import com.kidsability.automation.context.SharePointContext;
import com.kidsability.automation.context.secret.AzureCredentials;
import com.kidsability.automation.factory.WorkBookFactory;
import com.kidsability.automation.model.Client;
import com.kidsability.automation.pojo.ExcelCell;
import com.kidsability.automation.util.GraphApiUtil;
import com.microsoft.graph.core.GraphErrorCodes;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.*;
import com.microsoft.graph.options.HeaderOption;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.WorkbookCreateSessionRequest;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class SharePointService {
    private final GraphServiceClient graphServiceClient;
    private final SharePointContext sharePointContext;
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

    public CompletableFuture<DriveItem> getDriveItemByPathFuture(String path) throws RuntimeException {
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/root:/" + path;
        try {
            CompletableFuture<DriveItem> driveItemFuture = (CompletableFuture<DriveItem>) graphServiceClient
                    .customRequest(url,DriveItem.class)
                    .buildRequest()
                    .getAsync();
            return driveItemFuture;
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

    public CompletableFuture<DriveItem> copyItemFuture(DriveItem toCopy, DriveItem destParent, String copiedFileName) {
        ItemReference parentReference = new ItemReference();
        parentReference.siteId = sharePointContext.getSiteId();
        parentReference.id = destParent.id;

       CompletableFuture<DriveItem> res =  graphServiceClient.sites(sharePointContext.getSiteId())
               .drive()
               .items(toCopy.id)
                .copy(DriveItemCopyParameterSet
                        .newBuilder()
                        .withName(copiedFileName)
                        .withParentReference(parentReference)
                        .build())
                .buildRequest()
                .postAsync();
       return res;
    }

    public void awaitCopyCompletion(DriveItem copiedItem) {
        String url = copiedItem
                .additionalDataManager()
                .get("graphResponseHeaders").getAsJsonObject()
                .get("location")
                .getAsJsonArray()
                .get(0)
                .getAsString();
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();
        while(true) {
            String jsonRes = restTemplate.getForObject(url, String.class);
            try {
                Map<String, String> map = mapper.readValue(jsonRes, Map.class);
                if(map.get("status").equals("completed")) {
                    copiedItem.id = map.get("resourceId");
                    break;
                }
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

    }

    public CompletableFuture<WorkbookRange> getWorkBookRange(DriveItem excelDriveItem, String rangeAddress) {
        return graphServiceClient
                .sites(sharePointContext.getSiteId())
                .drive()
                .items(excelDriveItem.id)
                .workbook()
                .worksheets("Sheet1")
                .range(WorkbookWorksheetRangeParameterSet
                        .newBuilder()
                        .withAddress(rangeAddress)
                        .build()
                )
                .buildRequest()
                .getAsync();
    }

    public CompletableFuture<WorkbookRange> getWorkBookRange(DriveItem excelDriveItem, String rangeAddress, String workSheetName, String workbookSessionId) {
        LinkedList<Option> requestOptions = new LinkedList<>();
        requestOptions.add(new HeaderOption("workbook-session-id", workbookSessionId));
        return graphServiceClient
                .sites(sharePointContext.getSiteId())
                .drive()
                .items(excelDriveItem.id)
                .workbook()
                .worksheets(workSheetName)
                .range(WorkbookWorksheetRangeParameterSet
                        .newBuilder()
                        .withAddress(rangeAddress)
                        .build()
                )
                .buildRequest(requestOptions)
                .getAsync();
    }

    public WorkbookRange updateWorkBookRange(DriveItem excelDriveItem, String rangeAddress,
                                                                WorkbookRange updatedRange) {
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/"
                + excelDriveItem.id + "/workbook/worksheets('Sheet1')/range(address='" + rangeAddress + "')";
        JsonObject body = new JsonObject();
        body.add("formulas", updatedRange.formulas);
        return (WorkbookRange) graphServiceClient
                .customRequest(url, WorkbookRange.class)
                .buildRequest()
                .patch(body);
    }

    public WorkbookRange updateWorkBookRange(DriveItem excelDriveItem, String rangeAddress,
                                             WorkbookRange updatedRange, String workSheetName, String workbookSessionId) {
        LinkedList<Option> requestOptions = new LinkedList<>();
        requestOptions.add(new HeaderOption("workbook-session-id", workbookSessionId));
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/"
                + excelDriveItem.id + "/workbook/worksheets('" + workSheetName + "')/range(address='" + rangeAddress + "')";
        JsonObject body = new JsonObject();
        body.add("formulas", updatedRange.formulas);
        return (WorkbookRange) graphServiceClient
                .customRequest(url, WorkbookRange.class)
                .buildRequest(requestOptions)
                .patch(body);
    }

    public void clearWorkBookRange(DriveItem excelDriveItem, String rangeAddress) {
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/"
                + excelDriveItem.id + "/workbook/worksheets('Sheet1')/range(address='" + rangeAddress + "')/clear";

        JsonObject body = new JsonObject();
        body.add("applyTo", new JsonPrimitive("All"));

        graphServiceClient
                .customRequest(url)
                .buildRequest()
                .post(body);
    }

    public CompletableFuture<WorkbookRangeFill> getWorkBookCellFill(DriveItem excelDriveItem, String cellAddress) {
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/"
                + excelDriveItem.id + "/workbook/worksheets('Sheet1')/range(address='" + cellAddress + "')/format/fill";
        var res = (CompletableFuture<WorkbookRangeFill>) graphServiceClient
                .customRequest(url, WorkbookRangeFill.class)
                .buildRequest()
                .getAsync();

        return res;
    }

    public CompletableFuture<WorkbookRangeFont> getWorkBookCellFont(DriveItem excelDriveItem, String cellAddress) {
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/"
                + excelDriveItem.id + "/workbook/worksheets('Sheet1')/range(address='" + cellAddress + "')/format/font";
        var res = (CompletableFuture<WorkbookRangeFont>) graphServiceClient
                .customRequest(url, WorkbookRangeFont.class)
                .buildRequest()
                .getAsync();

        return res;
    }

    public CompletableFuture<WorkbookRangeFormat> getWorkBookCellFormat(DriveItem excelDriveItem, String cellAddress) {
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/"
                + excelDriveItem.id + "/workbook/worksheets('Sheet1')/range(address='" + cellAddress + "')/format";
        return  graphServiceClient
                .customRequest(url, WorkbookRangeFormat.class)
                .buildRequest()
                .getAsync();
    }

    public CompletableFuture<WorkbookRangeBorder> getWorkBookCellBorders(DriveItem excelDriveItem, String cellAddress) {
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/"
                + excelDriveItem.id + "/workbook/worksheets('Sheet1')/range(address='" + cellAddress + "')/format/borders";
        return  graphServiceClient
                .customRequest(url, WorkbookRangeBorder.class)
                .buildRequest()
                .getAsync();
    }


    public void updateWorkBookCellFont(DriveItem excelDriveItem, String cellAddress, WorkbookRangeFont workbookRangeFont,
                                       String workbookSessionId) {
        LinkedList<Option> requestOptions = new LinkedList<>();
        requestOptions.add(new HeaderOption("workbook-session-id", workbookSessionId));
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/"
                + excelDriveItem.id + "/workbook/worksheets('Sheet1')/range(address='" + cellAddress + "')/format/font";
        graphServiceClient
                .customRequest(url)
                .buildRequest(requestOptions)
                .patch(workbookRangeFont);
    }

    public void updateWorkBookCellFill(DriveItem excelDriveItem, String cellAddress, WorkbookRangeFill workbookRangeFill, String workSheetName,
                                       String workbookSessionId) {
        LinkedList<Option> requestOptions = new LinkedList<>();
        requestOptions.add(new HeaderOption("workbook-session-id", workbookSessionId));
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/"
                + excelDriveItem.id + "/workbook/worksheets('" + workSheetName + "')/range(address='" + cellAddress + "')/format/fill";
        graphServiceClient
                .customRequest(url)
                .buildRequest(requestOptions)
                .patch(workbookRangeFill);
    }

    public void updateWorkBookCellFormat(DriveItem excelDriveItem, String cellAddress, WorkbookRangeFormat workbookRangeFormat
            , String workbookSessionId) {
        LinkedList<Option> requestOptions = new LinkedList<>();
        requestOptions.add(new HeaderOption("workbook-session-id", workbookSessionId));
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/"
                + excelDriveItem.id + "/workbook/worksheets('Sheet1')/range(address='" + cellAddress + "')/format";
        graphServiceClient
                .customRequest(url)
                .buildRequest(requestOptions)
                .patch(workbookRangeFormat);
    }

    public void updateWorkBookCellBorders(DriveItem excelDriveItem, String cellAddress, List<WorkbookRangeBorder> workbookRangeBorders,
                                          String workbookSessionId) {
        LinkedList<Option> requestOptions = new LinkedList<>();
        requestOptions.add(new HeaderOption("workbook-session-id", workbookSessionId));
        for(WorkbookRangeBorder workbookRangeBorder : workbookRangeBorders) {
            var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/"
                    + excelDriveItem.id + "/workbook/worksheets('Sheet1')/range(address='" + cellAddress + "')/format/borders/"
                    + workbookRangeBorder.sideIndex;
            graphServiceClient
                    .customRequest(url)
                    .buildRequest(requestOptions)
                    .patch(workbookRangeBorder);
        }
    }

    public String getExcelSessionId(DriveItem excelDriveItem) {
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/"
                + excelDriveItem.id + "/workbook/createSession";
        JsonObject res = (JsonObject) graphServiceClient.customRequest(url, JsonObject.class)
                .buildRequest()
                .post(WorkbookCreateSessionParameterSet
                        .newBuilder()
                        .withPersistChanges(true)
                        .build()
                );
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = new Gson().fromJson(res.toString(), type);
        return (String) map.get("id");
    }

    public void mergeCells(DriveItem excelDriveItem, String rangeAddress, String workbookSessionId) {
        LinkedList<Option> requestOptions = new LinkedList<>();
        requestOptions.add(new HeaderOption("workbook-session-id", workbookSessionId));
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/"
                + excelDriveItem.id + "/workbook/worksheets('Sheet1')/range(address='" + rangeAddress + "')/merge";
        graphServiceClient
                .customRequest(url)
                .buildRequest(requestOptions)
                .post(WorkbookRangeMergeParameterSet
                        .newBuilder()
                        .withAcross(false)
                        .build());
    }

    public void shiftCellsDown(DriveItem excelDriveItem, String rangeAddress, String workbookSessionId) {
        LinkedList<Option> requestOptions = new LinkedList<>();
        requestOptions.add(new HeaderOption("workbook-session-id", workbookSessionId));
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/"
                + excelDriveItem.id + "/workbook/worksheets('Sheet1')/range(address='" + rangeAddress + "')/insert";

        var shift = "Down";

        graphServiceClient
                .customRequest(url)
                .buildRequest(requestOptions)
                .post(WorkbookRangeInsertParameterSet
                        .newBuilder()
                        .withShift(shift)
                        .build());

    }

    public void updateWorkSheetName(DriveItem excelDriveItem, String oldWorkSheetName, String newWorkSheetName, String workbookSessionId) {
        LinkedList<Option> requestOptions = new LinkedList<>();
        requestOptions.add(new HeaderOption("workbook-session-id", workbookSessionId));
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/"
                + excelDriveItem.id + "/workbook/worksheets/" + oldWorkSheetName;

        WorkbookWorksheet workbookWorksheet = new WorkbookWorksheet();
        workbookWorksheet.name = newWorkSheetName;

        graphServiceClient
                .customRequest(url)
                .buildRequest(requestOptions)
                .patch(workbookWorksheet);
    }


}
