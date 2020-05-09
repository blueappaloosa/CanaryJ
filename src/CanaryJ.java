/**
 * CanaryJ is a BlueJ plugin that simplifies the process of building Minecraft
 * plugins for CanaryMod.
 *
 * Copyright (C) 2014 Erica Liszewski
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */
import bluej.extensions.*;
import bluej.extensions.ClassNotFoundException;
import bluej.extensions.event.CompileEvent;
import bluej.extensions.event.CompileListener;
import java.awt.BorderLayout;
import static java.awt.Component.LEFT_ALIGNMENT;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Erica Liszewski November 2014
 *
 * Plugin for BlueJ allowing easy integration with CanaryMod for Minecraft.
 */
public class CanaryJ extends Extension {

    public void startup(BlueJ bluej) {
        // Register a "preferences" panel generator
        CanaryPreferences myPreferences = new CanaryPreferences(bluej);
        bluej.setPreferenceGenerator(myPreferences);

        //add CanaryCompile option to tools menu
        bluej.setMenuGenerator(new CanaryMenuBuilder(bluej));
        File lib = bluej.getSystemLibDir();

        boolean added = true;

        if (!addFile("canaryplugin.tmpl", lib, "Canary Plugin")) {
            added = false;
        }
        
        if (!addFile("canarypluginlistener.tmpl", lib, "Canary PluginListener")) {
            added = false;
        }

        //EZPlugin: comment out this if statement if not using   
         if (!addFile("ezplugin.tmpl", lib, "EZPlugin")) {
         added = false;
         }
        
         
        if (!added) {
            //if we couldn't add the templates
            //pop up dialog about manually adding templates
            JOptionPane.showMessageDialog(null,
                    "Could not create template files!\nSee documentation for creating these manually.",
                    "Failed to Create Templates",
                    JOptionPane.PLAIN_MESSAGE);
        }

    }

    public boolean addFile(String filename, File lib, String name) {

        File temp1 = new File(lib.getPath() + File.separator + "english" + File.separator + "templates" + File.separator + "newclass" + File.separator + filename);
        // System.out.println(temp1.getPath());
        
        
        
        if (!temp1.exists()) {
            System.out.println(filename + " not here");

            InputStream stream = null;
            OutputStream resStreamOut = null;
            String templaterFolder;
            try {
                stream = CanaryJ.class.getResourceAsStream(filename);
                if (stream == null) {
                    /*
                    System.out.println("Failed to open");
                    JOptionPane.showMessageDialog(null,
                    "Failed to Create Stream",
                    "Failed to Create Templates",
                    JOptionPane.WARNING_MESSAGE);
                    */
                    return false;
                }

                int readBytes;
                byte[] buffer = new byte[4096];
                templaterFolder = temp1.getPath();
                resStreamOut = new FileOutputStream(temp1);
                while ((readBytes = stream.read(buffer)) > 0) {
                    resStreamOut.write(buffer, 0, readBytes);
                }

                FileOutputStream labelstream = new FileOutputStream(new File(lib.getPath() + File.separator + "english" + File.separator + "labels"), true);
                OutputStreamWriter osw = new OutputStreamWriter(labelstream);
                Writer w = new BufferedWriter(osw);

                String classname = filename.substring(0, filename.indexOf("."));

                w.write("\npkgmgr.newClass." + classname + "=" + name);

                w.close();
                osw.close();
                labelstream.close();
                stream.close();
                resStreamOut.close();
                
              //  temp1 = null; w = null; System.gc();

            } catch (Exception ex) {
                /*
                System.out.println("Failed to copy");
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    ex.toString(),
                    "Failed to Create Templates",
                    JOptionPane.WARNING_MESSAGE);
                */
                return false;
            } 
        }
        return true;
    }

    /*
     * Returns the version number of this extension
     */
    public String getVersion() {
        return ("2014.11");

    }
    /*
     * Returns the user-visible name of this extension
     */

    public String getName() {
        return ("CanaryJ");
    }

    /*
     * This method must decide if this Extension is compatible with the 
     * current release of the BlueJ Extensions API
     */
    public boolean isCompatible() {
        //TODO fix this
        return true;
    }

    public String getDescription() {
        return ("A plugin to make it easier to build plugins for Minecraft using CanaryMod.");
    }

}

/*
 * Creates the menu in Preferences Dialog
 * allows users to enter the path to CanaryMod's plugins directory
 * for quick builds
 * also has them enter their name for the Canary.inf file
 */
class CanaryPreferences implements PreferenceGenerator {

    private JPanel myPanel;
    private JTextField path;
    private JTextField username;
    private JFileChooser canarypath;
    private JButton browse;
    private BlueJ bluej;
    public static final String CANARY_PATH = "Path-to-CanaryMod";
    public static final String USER_NAME = "User-Name";

    // Construct the panel, and initialise it from any stored values
    public CanaryPreferences(BlueJ bluej) {
        this.bluej = bluej;
        myPanel = new JPanel();
        myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
        JPanel pathPanel = new JPanel();
        pathPanel.setAlignmentY(0);
        pathPanel.add(new JLabel("Path to CanaryMod plugin folder:"));
        path = new JTextField(45);
        pathPanel.add(path);
        canarypath = new JFileChooser();
        canarypath.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        browse = new JButton("Browse");
        browse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnVal = canarypath.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    path.setText(canarypath.getSelectedFile().getAbsolutePath());

                }
            }
        });
        pathPanel.add(browse);
        myPanel.add(pathPanel);

        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.setAlignmentY(0);
        namePanel.add(new JLabel("Author Name:"));
        username = new JTextField(55);
        namePanel.add(username);

        myPanel.add(namePanel);

        // Load the default value
        loadValues();
    }

    public JPanel getPanel() {
        return myPanel;
    }

    public void saveValues() {
        // Save the preference value in the BlueJ properties file
        bluej.setExtensionPropertyString(CANARY_PATH, path.getText());
        bluej.setExtensionPropertyString(USER_NAME, username.getText());

    }

    public void loadValues() {
        // Load the property value from the BlueJ properties file, default to an empty string
        path.setText(bluej.getExtensionPropertyString(CANARY_PATH, ""));
        username.setText(bluej.getExtensionPropertyString(USER_NAME, ""));
    }
}

class CanaryMenuBuilder extends MenuGenerator {

    private BPackage curPackage;
    private BlueJ bluej;

    String savepath = "";
    String savename = "";
    String username = "";
    String projname = "";
    String version = "1.0";
    
    CanaryCompileListener compileListener = new CanaryCompileListener();

    public CanaryMenuBuilder(BlueJ bluej) {
        super();
        this.bluej = bluej;
        savepath = bluej.getExtensionPropertyString("Path-to-CanaryMod", "");
        username = bluej.getExtensionPropertyString("User-Name", "");
    }

    public JMenuItem getToolsMenuItem(BPackage aPackage) {
        return new JMenuItem(new CanaryCompileAction("Build for Canary"));
    }

    public void notifyPostToolsMenu(BPackage bp, JMenuItem jmi) {
        curPackage = bp;
    }

    // The nested class that instantiates the different (simple) menus.
    class CanaryCompileAction extends AbstractAction {

        public CanaryCompileAction(String menuName) {
            putValue(AbstractAction.NAME, menuName);
        }

        //the action that happens when we click "Build for Canary"
        public void actionPerformed(ActionEvent anEvent) {
            System.out.println("Canary Compile!");

            try {
                savename = username.trim().replace(" ", "_") + "_" + curPackage.getProject().getName();
                projname = curPackage.getName();
            } catch (ProjectNotOpenException | PackageNotFoundException e) {
            }

            //verify project, name make sure to chop off .jar
            //verify author
            //verify version
            if (savepath.equals("")) {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int val = fc.showSaveDialog(bluej.getCurrentFrame());
                if (val == JFileChooser.APPROVE_OPTION) {
                    savepath = fc.getSelectedFile().getAbsolutePath();
                }
            }

            //popup simple dialog here
            CanaryCompileDialog dialog = new CanaryCompileDialog(false);
            dialog.makeDialog();

        }

    }

    /*
     * Listener for Compiler events. In this case were really only interested in
     * the compile being successful, so we can build the jar
     */
    class CanaryCompileListener implements CompileListener {

        public void compileFailed(CompileEvent e) {
            //remove this as a listener, because we don't care about compile events
            //that weren't started by our "build for canary" option
            bluej.removeCompileListener(this);
        }

        public void compileSucceeded(CompileEvent e) {
            //the compile has succeeded, now we build the jar
            //jar file just takes everything that's in the project
            //source and all. This is so students can turn in the 
            //jar and the source is availible for grading
            System.out.println("Finished Canary Compile");
            
            //remove this as a listener, because we don't care about compile events
            //that weren't started by our "build for canary" option
            bluej.removeCompileListener(this);
            
            //create the Canary.inf file
            try {
            BClass main = null;

                BPackage[] packs = curPackage.getProject().getPackages();

                //search for the main class, this will extend Plugin
                for (int i = 0; i < packs.length; i++) {
                    //  System.out.println(packs[i].getName());
                    main = findMain(packs[i]);
                    if (main != null) {
                        break;
                    }
                }

                if (main != null) {

                    File inf = new File(curPackage.getProject().getDir().getPath() + File.separator + "Canary.inf");
                    FileOutputStream is = new FileOutputStream(inf);
                    OutputStreamWriter osw = new OutputStreamWriter(is);
                    Writer w = new BufferedWriter(osw);
                    w.write("main-class = " + main.getName() + "\n");
                    w.write("name = " + projname + "\n");
                    w.write("author = " + username + "\n");
                    w.write("version = " + version + "\n");
                    w.close();

                    osw.close();
                    is.close();

                } else {
                     //If we couldn't find a main class, return an error

                    //popup error message here
                    String message = "No class found that extends net.canarymod.plugin.Plugin";

                    //EZPlugin: comment out this next line statement if not using EZplugin
                    message = message += "\n or com.pragprog.ahmine.ez.EZPlugin";

                    JOptionPane.showMessageDialog(null,
                            "No Main Class",
                            message,
                            JOptionPane.PLAIN_MESSAGE);
                    return;

                }
            }catch (ProjectNotOpenException | IOException xe){
                return;
            }

            try {  //create the manifest file for the jar
                
                //  System.out.println(curPackage.getName());
                //  System.out.println(curPackage.getProject().getDir().getPath());

                File parentdir = new File(curPackage.getProject().getDir().getPath());
                Manifest manifest = new Manifest();
                manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
                JarOutputStream target = new JarOutputStream(new FileOutputStream(savepath + File.separator + savename + ".jar"), manifest);
  
                //now go through and add all the files to the jar
                add(parentdir, target, curPackage.getProject().getDir().getPath());
                target.close();

            } catch (IOException | ProjectNotOpenException e2) {
                System.out.println(e2.toString());
                return;
            }


            //popup success message here
            JOptionPane.showMessageDialog(null,
                    "Project built for CanaryMod",
                    "Success!",
                    JOptionPane.PLAIN_MESSAGE);
        }

        //this actually goes through the project and adds all the files to the jar
        public void add(File source, JarOutputStream target, String removeme)
                throws IOException {
            
           // System.out.println(source.getCanonicalPath());
            
            BufferedInputStream in = null;
            try {
                File parentDir = new File(removeme);
                //System.out.println("source path "+source.getCanonicalPath());
                // System.out.println("parentDir "+ parentDir.getCanonicalPath());
                File source2 = new File("/");
                if (! parentDir.getCanonicalPath().equals(source.getCanonicalPath())) {
                    //this removes the first part of the path, so the jar
                    //only contains relative paths (this is ideal)
                    source2 = new File(source.getCanonicalPath().substring(
                            parentDir.getCanonicalPath().length() + 1,
                            source.getCanonicalPath().length()));
                }
                //   System.out.println(source2.getPath());
                if (source.isDirectory()) {
                    //replace slashes because jars like / instead of \
                    String name = source2.getPath().replace("\\", "/");
                    if (!name.isEmpty()) {
                        if (!name.endsWith("/")) {
                            name += "/";
                        }
                        JarEntry entry = new JarEntry(name);
                        entry.setTime(source.lastModified());
                        target.putNextEntry(entry);
                        target.closeEntry();
                    }
                    //repeat for all files and directories in this one, so we 
                    //get everything
                    for (File nestedFile : source.listFiles()) {
                        add(nestedFile, target, removeme);
                    }
                    return;
                }

                JarEntry entry = new JarEntry(source2.getPath().replace("\\", "/"));
                entry.setTime(source.lastModified());
                target.putNextEntry(entry);

                //this part actually does the transferring of data into the jar
                in = new BufferedInputStream(new FileInputStream(source));

                byte[] buffer = new byte[2048];
                while (true) {
                    int count = in.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    target.write(buffer, 0, count);
                }
                target.closeEntry();
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }

        public void compileWarning(CompileEvent e) {
        }

        public void compileError(CompileEvent e) {
            /*
             File files[] = e.getFiles();
             for(int i=0; i<files.length; i++){
             System.out.println(files[i].getName());
             }
             */
        }

        public void compileStarted(CompileEvent e) {
        }
        
        public BClass findMain(BPackage pack) {
            try {
                BClass[] classes = pack.getClasses();

                for (int i = 0; i < classes.length; i++) {
                    //  System.out.println(classes[i].getName());
                    //  System.out.println(classes[i].getSuperclass().getName());

                    if (classes[i].getSuperclass().getName().equals("net.canarymod.plugin.Plugin")) {
                        return classes[i];
                    }

                    //EZPlugin: comment out this if statement if not using EZplugin
                    if (classes[i].getSuperclass().getName().equals("com.pragprog.ahmine.ez.EZPlugin")) {
                        return classes[i];
                    }
                    

                }
            } catch (PackageNotFoundException | ProjectNotOpenException | ClassNotFoundException e) {
            }
            return null;
        }

    }

    /*
     * Creates the dialog that pops up when "Build for Canary" is selected
     * allows users to enter a version number and name the jar
     * if no path to the plugins directory is set, it will ask where to save
     */
    class CanaryCompileDialog extends JDialog {

        JTextField userfield;
        JTextField projectfield;
        JTextField savefield;
        JTextField versionfield;
        Frame frame;

        public CanaryCompileDialog(boolean needpath) {
            super(bluej.getCurrentFrame(), "Build for CanaryMod", true);
            frame = bluej.getCurrentFrame();

        }

        public void makeDialog() {
            JPanel mainPanel = new JPanel(new BorderLayout());

            this.getContentPane().add(mainPanel);

            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
           
            /*
            JPanel labelPanel = new JPanel();
            labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
            labelPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 4));
            JPanel entryPanel = new JPanel();
            entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.Y_AXIS));
            entryPanel.setBorder(BorderFactory.createEmptyBorder(10, 4, 10, 10));
            
            mainPanel.add(labelPanel, BorderLayout.WEST);
            mainPanel.add(entryPanel, BorderLayout.EAST);
            */
            
            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            namePanel.setAlignmentY(0);
            namePanel.add(new JLabel("Author Name:"));
            userfield = new JTextField(25);
           // userfield.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            namePanel.add(userfield);
            mainPanel.add(namePanel);

            JPanel projPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            projPanel.setAlignmentY(0);
            projPanel.add(new JLabel("Plugin Name:"));
            projectfield = new JTextField(25);
            //projectfield.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            projPanel.add(projectfield);
            mainPanel.add(projPanel);

            JPanel versionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            versionPanel.setAlignmentY(0);
            versionPanel.add(new JLabel("Plugin Version:"));
            versionfield = new JTextField(25);
            //versionfield.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            versionPanel.add(versionfield);
            mainPanel.add(versionPanel);

            JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            savePanel.setAlignmentY(0);
            savePanel.add(new JLabel("Save As:"));
            savefield = new JTextField(25);
            //savefield.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            savePanel.add(savefield);
            mainPanel.add(savePanel);

            userfield.setText(username);
            projectfield.setText(projname);
            versionfield.setText(version);
            savefield.setText(savename + ".jar");

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            buttonPanel.setAlignmentX(LEFT_ALIGNMENT);

            JButton continueButton = new JButton("Continue");
            continueButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    doOK();
                }
            });

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    doCancel();
                }
            });

            buttonPanel.add(continueButton);
            buttonPanel.add(cancelButton);

            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            getRootPane().setDefaultButton(continueButton);

            pack();
            setLocationRelativeTo(frame);
            setVisible(true);
        }

        public void doOK() {
            username = userfield.getText();
            projname = projectfield.getText();
            version = versionfield.getText();
            savename = savefield.getText().substring(0, savefield.getText().indexOf(".jar"));

            userfield.setText(username);
            projectfield.setText(projname);
            versionfield.setText(version);
            savefield.setText(savename + ".jar");

            setVisible(false);

            if (savename.equals("")) {
                JOptionPane.showMessageDialog(null,
                        "No Filename",
                        "You must provide a save name!",
                        JOptionPane.PLAIN_MESSAGE);
            } else {
                doCompile();
            }
        }

        public void doCancel() {
            setVisible(false);
        }

        private void doCompile() {
            //add a listener so we know if the compile is successful
            bluej.addCompileListener(compileListener);

            try {
                
                //ask BlueJ to compile the project for us
                curPackage.compileAll(false);

            } catch (ProjectNotOpenException | PackageNotFoundException | CompilationNotStartedException e) {
                System.out.println(e.toString());
                bluej.removeCompileListener(compileListener);
            }
        }

        

    }
}
