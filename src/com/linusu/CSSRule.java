package com.linusu;

import com.linusu.CSSProperty;
import com.linusu.CSSProperty.*;

import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.AbstractMap.SimpleEntry;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;

public class CSSRule {
    
    protected String selector;
    protected List<Map.Entry> property;
    protected List<Map.Entry> output;
    
    protected final Pattern rgba = Pattern.compile("^rgba\\(([0-9]+), ([0-9]+), ([0-9]+), ([0-9.]+)\\)$");
    protected final Pattern linear_gradient = Pattern.compile("^linear-gradient\\(top, \\#([a-fA-F0-9]{3,6}) 0%, \\#([a-fA-F0-9]{3,6}) 100%\\)$");
    
    public CSSRule(String selector) {
        
        this.selector = selector.trim();
        this.property = new LinkedList();
        this.output = new LinkedList();
        
    }
    
    public void add(String property, String value) {
        this.property.add(new SimpleEntry<String, String>(property.trim(), value.trim()));
    }
    
    protected void out(String prefix, String property, String value) {
        this.output.add(new SimpleEntry<String, String>(prefix + property, value));
    }
    
    public void process() {
        
        for(Map.Entry<String, String> entry : property) {
            process(entry.getKey(), entry.getValue());
        }
        
    }
    
    protected void process(String p, String v) {
        
        switch(CSSProperty.fromString(p)) {
            case OPACITY:
                out("-khtml-", p, v);
                out("-moz-", p, v);
                break;
            case BORDER_RADIUS:
            case BOX_SHADOW:
            case COLUMN_COUNT:
            case COLUMN_FILL:
            case COLUMN_GAP:
            case COLUMN_RULE:
            case COLUMN_SPAN:
            case COLUMN_WIDTH:
                out("-webkit-", p, v);
                out("-moz-", p, v);
                break;
            case USER_SELECT:
                out("-webkit-", p, v);
                out("-khtml-", p, v);
                out("-moz-", p, v);
                out("-o-", p, v);
                break;
            case BACKGROUND_SIZE:
            case TRANSITION:
            case TRANSITION_DURATION:
            case TRANSITION_PROPERTY:
            case TRANSITION_DELAY:
            case TRANSITION_TIMING_FUNCTION:
                out("-webkit-", p, v);
                out("-moz-", p, v);
                out("-o-", p, v);
                break;
            default:
                
                Matcher m;
                
                m = rgba.matcher(v);
                if(m.find()) {
                    float f = Float.parseFloat(m.group(4));
                    if(f == 1.) {
                        v = "#" + RGB2HEX(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
                    } else if(f == 0.) {
                        v = "transparent";
                    } else {
                        out("", p, "#" + RGB2HEX(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3))));
                    }
                }
                
                m = linear_gradient.matcher(v);
                if(m.find()) {
                    
                    out("", p, "#" + m.group(1));
                    out("", p, "-moz-" + m.group(0));
                    out("", p, "-webkit-gradient(linear, left top, left bottom, color-stop(0%,#" + m.group(1) + "), color-stop(100%,#" + m.group(2) + ")");
                    out("", p, "-webkit-" + m.group(0));
                    out("", p, "-o-" + m.group(0));
                    out("", p, "-ms-" + m.group(0));
                    
                    if(p.equals("background") || p.equals("background-image")) {
                        out("", "filter", "progid:DXImageTransform.Microsoft.gradient(startColorstr='#" + HEX2HEX(m.group(1)) + "', endColorstr='#" + HEX2HEX(m.group(2)) + "', GradientType=0)");
                    }
                    
                }
                
                break;
        }
        
        out("", p, v);
        
    }
    
    public void print(FileOutputStream out) {
        try {
            
            boolean s = false;
            
            out.write("\n".getBytes("UTF-8"));
            out.write(selector.getBytes("UTF-8"));
            out.write(" {\n".getBytes("UTF-8"));
            
            for(Map.Entry<String, String> entry : output) {
                out.write((s?" ":"    ").getBytes("UTF-8"));
                out.write(entry.getKey().getBytes("UTF-8"));
                out.write(": ".getBytes("UTF-8"));
                out.write(entry.getValue().getBytes("UTF-8"));
                s = entry.getKey().startsWith("-");
                out.write((s?";":";\n").getBytes("UTF-8"));
            }
            
            out.write("}\n".getBytes("UTF-8"));
            
        } catch(UnsupportedEncodingException e) {
            // FIXME
        } catch(IOException e) {
            // FIXME
        }
    }
    
    static private String RGB2HEX(int r, int g, int b) {
        return (
            ((r < 16)?"0":"") + Integer.toHexString(r) +
            ((g < 16)?"0":"") + Integer.toHexString(g) +
            ((b < 16)?"0":"") + Integer.toHexString(b)
        );
    }
    
    static private String HEX2HEX(String hex) {
        return ((hex.length() == 6)?hex:(
            hex.substring(0, 1) + hex.substring(0, 1) + 
            hex.substring(1, 2) + hex.substring(1, 2) + 
            hex.substring(2, 3) + hex.substring(2, 3)
        ));
    }
    
}
