package com.signix.service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PdfService {

    @Value("${app.upload.dir}")
    private String uploadDir;
    public String mergeSignatureIntoPdf(String originaleFileName, String signatureImageBase64)throws IOException {
       String [] parts= signatureImageBase64.split(",");
       String base64data= parts[1];
       byte [] imageBytes = Base64.getDecoder().decode(base64data);

        Path originalPath= Paths.get(uploadDir,originaleFileName);
        PDDocument document= Loader.loadPDF(originalPath.toFile());
        PDImageXObject pdImageXObject= PDImageXObject.createFromByteArray(document,imageBytes,"signature");

        int lastPageIndex= document.getNumberOfPages()-1;
        PDPage page= document.getPage(lastPageIndex);

        PDPageContentStream contentStream= new PDPageContentStream(
                document,
                page,
                PDPageContentStream.AppendMode.APPEND,
                true, true
        );
        float x = 350;
        float y = 50;
        float width = 150;
        float height = 60;
        contentStream.drawImage(pdImageXObject,x,y,width,height);
        contentStream.close();

        String newFileName= UUID.randomUUID()+"_"+"signed"+"_"+originaleFileName;
        Path signedPath= Paths.get(uploadDir,newFileName);
        document.save(signedPath.toFile());
        document.close();
        return newFileName;
    }
}
