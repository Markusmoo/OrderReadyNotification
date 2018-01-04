package ca.tonsaker.orn.utility;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * Created by Markus Tonsaker on 2018-01-03.
 */
public class NumberFilter extends DocumentFilter{

    private boolean allowMisc = false;
    private int maxLength = -1;

    public NumberFilter(boolean allowMisc, int maxLength){
        super();
        this.allowMisc = allowMisc;
        this.maxLength = maxLength;
    }

    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException{

        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.insert(offset, string);

        if(test(sb.toString())){
            super.insertString(fb, offset, string, attr);
        }
    }

    private boolean test(String text){
        text = text.trim();
        if(text.isEmpty()) return true;
        if(allowMisc) {
            text = text.replace("(", "");
            text = text.replace(")", "");
            text = text.replace("-", "");
            text = text.replace("+", "");
            text = text.replace(" ", "");
            if(text.trim().isEmpty()) return true;
        }

        if(text.contains("d") || text.contains("D") || text.contains("f") || text.contains("F") || text.contains(".")) return false;

        try{
            Double.parseDouble(text);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }

    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException{

        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.replace(offset, offset + length, text);

        if(test(sb.toString()) && (maxLength < 0 || text.length() <= maxLength)){
            super.replace(fb, offset, length, text, attrs);
        }

    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException{
        Document doc = fb.getDocument();
        StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.delete(offset, offset + length);

        if(maxLength < 0 || sb.toString().length() <= maxLength){
            super.remove(fb, offset, length);
        }

    }
}
