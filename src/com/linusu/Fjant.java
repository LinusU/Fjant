package com.linusu;

import com.linusu.CSSRule;
import com.linusu.Indentation;

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
import java.io.UnsupportedEncodingException;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

public class Fjant extends Task {
    
    private File outputFile;
    private List<ResourceCollection> sourceRC;
    
    private FileOutputStream outputStream;
    
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
    
    public void execute() throws BuildException {
        
        if(this.outputFile == null) {
            throw new BuildException("Output attribute must be set");
        }
        
        Resource[] files = findSourceFiles();
        
        if(!updateNeeded(files)) {
            log("None of the files changed, processing skipped.");
            return ;
        }
        
        try {
            outputStream = new FileOutputStream(this.outputFile);
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
            int comment = 0;
            StringBuilder sb = new StringBuilder();
            StringBuilder sbc = new StringBuilder();
            CSSRule rule = new CSSRule("");
            Indentation indent = new Indentation();
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
                
                switch(comment) {
                    case 0:
                        if(c == '/') {
                            comment = 1;
                            continue;
                        }
                        break;
                    case 1:
                        if(c == '*') {
                            comment = 2;
                            continue;
                        } else {
                            sb.append('/');
                            comment = 0;
                        }
                        break;
                    case 2:
                        if(c == '*') {
                            comment = 3;
                        } else {
                            sbc.append(c);
                        }
                        continue;
                    case 3:
                        if(c == '/') {
                            comment = 0;
                            if(state == 0) {
                            	writeOut("\n/*" + sbc.toString() + "*/\n");
                            } else {
                                rule.add("-fjant-comment", sbc.toString());
                            }
                            sbc = new StringBuilder();
                        } else {
                            sbc.append('*');
                            comment = 2;
                        }
                        continue;
                }
                
                switch(state) {
                    case 0:
                        if(c == '{') {
                            state = 1;
                            rule = new CSSRule(sb.toString());
                            sb = new StringBuilder();
                        } else if(c == '@') {
                        	if(!sb.toString().trim().equals("")) {
                        		throw new BuildException("Possibly malformed CSS");
                        	}
                        	state = 3;
                        	sb = new StringBuilder();
                        } else if(c == '}') {
                        	if(indent.value() == 0) {
                        		sb.append(c);
                        	} else {
                        		indent.decrease();
                        		writeOut("\n}\n");
                        		sb = new StringBuilder();
                        	}
                        } else {
                            sb.append(c);
                        }
                        break;
                    case 1:
                        if(c == '}') {
                            state = 0;
                            rule.process();
                            rule.print(outputStream, indent);
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
                            rule.print(outputStream, indent);
                            sb = new StringBuilder();
                        } else {
                            sb.append(c);
                        }
                        break;
                    case 3:
                    	if(c == '{' || c == ';') {
                    		state = 0;
                    		writeOut("\n@" + sb.toString() + c + "\n");
                    		if(c == '{') { indent.increase(); }
                            sb = new StringBuilder();
                    	} else {
	                    	sb.append(c);
                    	}
                    	break;
                }
                
                
            }
            
        }
        
    }
    
    private void writeOut(String str) throws BuildException {
    	try {
            outputStream.write(str.getBytes("UTF-8"));
        } catch(UnsupportedEncodingException e) {
            throw new BuildException("Error writing to output-file");
        } catch(IOException e) {
            throw new BuildException("Error writing to output-file");
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
