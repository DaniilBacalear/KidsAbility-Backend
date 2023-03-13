package com.kidsability.automation.service;

import com.kidsability.automation.context.SharePointContext;
import com.kidsability.automation.context.secret.AzureCredentials;
import com.kidsability.automation.util.GraphApiUtil;
import com.microsoft.graph.core.GraphErrorCodes;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.requests.GraphServiceClient;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Service
public class SharePointService {
    private GraphServiceClient graphServiceClient;
    private SharePointContext sharePointContext;
    public SharePointService(SharePointContext sharePointContext, AzureCredentials azureCredentials) {
        this.graphServiceClient = GraphApiUtil.getGraphClient(azureCredentials);
        this.sharePointContext = sharePointContext;
    }
    public DriveItem getDriveItem(String path) throws RuntimeException {
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

    public String getBase64Img(DriveItem driveItem) throws IOException {
        var url = "/sites/" + sharePointContext.getSiteId() + "/drive/items/" + driveItem.id + "/content";
        InputStream inputStream = (InputStream) graphServiceClient
                .customRequest(url, InputStream.class)
                .buildRequest()
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



}
