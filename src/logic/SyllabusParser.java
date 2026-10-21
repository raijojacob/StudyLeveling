package logic;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SyllabusParser {

    public List<String> extractMilestones(File pdfFile) {
        List<String> milestones = new ArrayList<>();
        
        // PDFBox 3.0.8 uses Loader.loadPDF(pdfFile)
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String fullText = stripper.getText(document);
            
            String[] lines = fullText.split("\\r?\\n");
            
            for (String line : lines) {
                String upperLine = line.trim().toUpperCase();
                if (upperLine.startsWith("MODULE") || 
                    upperLine.startsWith("WEEK") || 
                    upperLine.startsWith("CHAPTER")) {
                    
                    if (line.length() < 100) {
                        milestones.add("Study: " + line.trim());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read PDF file.");
            e.printStackTrace();
        }
        
        return milestones;
    }
}