package com.linusu;

import com.linusu.CSSRule;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

public class Fjant extends Task {
    
    private File outputFile;
    private List<ResourceCollection> sourceRC;
    
    public Fjant() {
        this.sourceRC = new LinkedList();
    }
    
    public void setOutput(File value) {
        this.outputFile = value;
    }
    
    public void addFileList(FileList list) {
        this.sourceRC.add(list);
    }
    
    public void addFileSet(FileSet set) {
        this.sourceRC.add(set);
    }
    
    public void addPath(Path set) {
        this.sourceRC.add(set);
    }
    
    public void execute() {
        
        if(this.outputFile == null) {
            throw new BuildException("Output attribute must be set");
        }
        
        Resource[] files = findSourceFiles();
        
        if(!updateNeeded(files)) {
            log("None of the files changed, processing skipped.");
            return ;
        }
        
        FileOutputStream out;
        
        try {
            out = new FileOutputStream(this.outputFile);
        } catch(FileNotFoundException e) {
            throw new BuildException("Output file not found");
        }
        
        log("Processing " + files.length + " file" + (files.length > 1?"s":"") + ".");
        
        for(Resource res : files) {
            
            InputStream in;
            
            try {
                in = res.getInputStream();
            } catch(IOException e) {
                throw new BuildException("Error opening input-file");
            }
            
            int state = 0;
            StringBuilder sb = new StringBuilder();
            CSSRule rule = new CSSRule("");
            String property = "";
            
            char c = 0;
            
            while(true) {
                
                try {
                    int i = in.read();
                    if(i == -1) { break; }
                    c = (char) i;
                } catch(IOException e) {
                    throw new BuildException("Error reading input-file");
                }
                
                switch(state) {
                    case 0:
                        if(c == '{') {
                            state = 1;
                            rule = new CSSRule(sb.toString());
                            sb = new StringBuilder();
                        } else {
                            sb.append(c);
                        }
                        break;
                    case 1:
                        if(c == '}') {
                            state = 0;
                            rule.process();
                            rule.print(out);
                            sb = new StringBuilder();
                        } else if(c == ':') {
                            state = 2;
                            property = sb.toString();
                            sb = new StringBuilder();
                        } else {
                            sb.append(c);
                        }
                        break;
                    case 2:
                        if(c == ';') {
                            state = 1;
                            rule.add(property, sb.toString());
                            sb = new StringBuilder();
                        } else if(c == '}') {
                            state = 0;
                            rule.add(property, sb.toString());
                            rule.process();
                            rule.print(out);
                            sb = new StringBuilder();
                        } else {
                            sb.append(c);
                        }
                        break;
                }
                
                
            }
            
        }
        
    }
    
    private Resource[] findSourceFiles() {
        
        List<Resource> files = new LinkedList<Resource>();
        
        for(ResourceCollection rc : this.sourceRC) {
            
            Iterator<Resource> i = rc.iterator();
            
            while(i.hasNext()) {
                files.add(i.next());
            }
            
        }
        
        return files.toArray(new Resource[files.size()]);
    }
    
    private boolean updateNeeded(Resource[] sources) {
        
        long lastRun = outputFile.lastModified();
        
        for(Resource res : sources) {
            
            long t = res.getLastModified();
            
            if(t == 0) {
                // File is absent
                return true;
            }
            
            if(t >= lastRun) {
                return true;
            }
            
        }
        
        return false;
    }
    
}
