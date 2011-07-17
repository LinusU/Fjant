package com.linusu;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

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
            
            BufferedReader in;
            
            try {
                in = new BufferedReader(new InputStreamReader(res.getInputStream()));
            } catch(IOException e) {
                throw new BuildException("Error opening input-file");
            }
            
            String line;
            
            try {
                line = in.readLine();
            } catch(IOException e) {
                throw new BuildException("Error reading line");
            }
            
            while(line != null) {
                
                line = line.replaceAll("((opacity):([^\\;]+);)", "-khtml-$1 -moz-$1 $1");
                line = line.replaceAll("((border-radius|box-shadow):([^\\;]+);)", "-webkit-$1 -moz-$1 $1");
                line = line.replaceAll("((user-select):([^\\;]+);)", "-webkit-$1 -khtml-$1 -moz-$1 -o-$1 $1");
                line = line.replaceAll("((background-size):([^\\;]+);)", "-webkit-$1 -moz-$1 -o-$1 $1");
                line = line.replaceAll("(transition-(duration|property|delay|timing-function):([^\\;]+);)", "-webkit-$1 -moz-$1 -o-$1 $1");
                line = line.replaceAll("(column-(count|fill|gap|rule|span|width):([^\\;]+);)", "-webkit-$1 -moz-$1 $1");
                line = line.replaceAll("(background): (linear-gradient\\(top, (\\#[a-fA-F0-9]{3,6}) 0%, (\\#[a-fA-F0-9]{3,6}) 100%\\));", "$1: $3; $1: -moz-$2; $1: -webkit-gradient(linear, left top, left bottom, color-stop(0%,$3), color-stop(100%,$4)); $1: -webkit-$2; $1: -o-$2; $1: -ms-$2; filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='$3', endColorstr='$4',GradientType=0 ); $1: $2;");
                line = line.replaceAll("([a-z-]+): rgba\\(([0-9]+, [0-9]+, [0-9]+), 1\\);", "$1: rgb($2);");
                line = line.replaceAll("([a-z-]+): rgba\\(([0-9]+, [0-9]+, [0-9]+), ([0-9.]+)\\);", "$1: rgb($2); $1: rgba($2, $3);");
                
                try {
                    out.write(line.getBytes("UTF-8"));
                    out.write('\n');
                } catch(IOException e) {
                    throw new BuildException("Error writing line");
                }
                
                try {
                    line = in.readLine();
                } catch(IOException e) {
                    throw new BuildException("Error reading line");
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
