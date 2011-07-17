package com.linusu;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileList;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import java.util.List;
import java.util.LinkedList;

public class Fjant extends Task {
    
    private File outputFile;
    private List<FileList> sourceFileLists;
    
    public Fjant() {
        this.sourceFileLists = new LinkedList();
    }
    
    public void setOutput(File value) {
        this.outputFile = value;
    }
    
    public void addSources(FileList list) {
        this.sourceFileLists.add(list);
    }
    
    public void execute() {
        
        if(this.outputFile == null) {
            throw new BuildException("Output attribute must be set");
        }
        
        File[] files = findSourceFiles();
        
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
        
        for(File file : files) {
            
            BufferedReader in;
            
            try {
                in = new BufferedReader(new FileReader(file));
            } catch(FileNotFoundException e) {
                throw new BuildException("Input file not found");
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
    
    private File[] findSourceFiles() {
        
        List<File> files = new LinkedList<File>();
        
        for(FileList list : this.sourceFileLists) {
            
            File dir = list.getDir(getProject());
            
            for(String included : list.getFiles(getProject())) {
                files.add(new File(dir, included));
            }
            
        }
        
        return files.toArray(new File[files.size()]);
    }
    
    private boolean updateNeeded(File[] sources) {
        
        long lastRun = outputFile.lastModified();
        
        for(File file : sources) {
            
            long t = file.lastModified();
            
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
