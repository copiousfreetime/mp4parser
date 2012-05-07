package com.googlecode.mp4parser.tools.smoothstreamingfragmenter;


import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.smoothstreaming.FlatPackageWriterImpl;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.FileOptionHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class FragmentFileSet {
    private static Logger LOG = Logger.getLogger(FragmentFileSet.class.getName());

    @Argument(required = true, multiValued = true, handler = FileOptionHandler.class, usage = "MP4 input files", metaVar = "in_1.mp4, in_2.mp4, ...")
    protected List<File> inputFiles;

    @Option(name = "--outputdir", aliases = "-o",
            usage = "output directory - if no output directory is given the " +
                    "current working directory is used.",
            metaVar = "PATH")
    protected File outputDir = new File("./smooth");

    @Option(name = "--debug", aliases = "-d", usage = "output files in between the stages of the process")
    boolean debug;

    public static void main(String[] args) throws IOException {
        FragmentFileSet fragmentFileSet = new FragmentFileSet();
        fragmentFileSet.parseCmdLine(args);
        fragmentFileSet.run();
    }

    public void run() throws IOException {
        FlatPackageWriterImpl flatPackageWriter = new FlatPackageWriterImpl();
        flatPackageWriter.setWriteSingleFile(debug);
        flatPackageWriter.setOutputDirectory(outputDir);
        Movie movie = new Movie();
        for (File input : inputFiles) {
            Movie m = MovieCreator.build(new FileInputStream(input).getChannel());
            for (Track track : m.getTracks()) {
                movie.addTrack(track);
            }

        }
        flatPackageWriter.write(movie);
    }

    private void parseCmdLine(String args[]) {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(80); // width of the error display area


        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {

            System.err.println(e.getMessage());
            System.err.println();
            // print the list of available options
            System.err.println("java -jar smooth-streaming-fragmenter-version.jar [options] in_1.mp4, in_2.mp4, ...");
            System.err.println();
            parser.printUsage(System.err);
            System.err.println();
            System.exit(1);
        }

    }

}
