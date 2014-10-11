/*
 * Copyright 2014 Dhruv Bansal <shrub.vandal@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dhruvbansal.plutus;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Validates the input blockchain data in a given directory is usable and
 * provides some summary statistics.
 *
 * @author Dhruv Bansal <shrub.vandal@gmail.com>
 */
public class DataValidator {
    
    /**
     * Run the DataValidator class.
     * 
     * @param argv the command-line arguments
     */
    public static void main(String[] argv) {
        if (argv.length < 1) {
            System.err.println("Pass an input directory to validate as the first argument.");
            System.exit(FAILURE_CODE);
        }
        DataValidator dataValidator = new DataValidator(argv[0]);
        dataValidator.validate();
        try {
            dataValidator.summarize();
        } catch (IOException e) {
            System.err.println("ERROR " + e.toString());
        }
        System.exit(0);
    }

    /**
     * Exit code when validation fails.
     */
    static final int FAILURE_CODE = 1;

    /**
     * The directory to validate.
     */
    public Path directory;

    /**
     * List of the blockchain data types.
     */
    static final String[] DATA_TYPES = {"blocks", "transactions", "inputs", "outputs"};

    /**
     * Initialize a new DataValidator examining the given directory.
     *
     * @param directory
     */
    public DataValidator(String directory) {
        this.directory = Paths.get(directory).toAbsolutePath();
    }

    /**
     * Validate the directory.
     */
    public void validate() {
        if (!Files.isDirectory(directory)) {
            fail(directory + " is not a directory or cannot be read");
        }
        for (String dataType : DATA_TYPES) {
            Path path = dataFilePath(dataType);
            if (!Files.isRegularFile(path)) {
                fail("Expected " + path.getParent() + " to contain a file " + path.getFileName());
            }
        }
        System.out.println("Directory " + directory + " contains valid blockchain input.");
    }

    /**
     * Summarize the data in the directory.
     */
    public void summarize() throws IOException {
        System.out.println("Summary of file line counts:");
        for (String dataType : DATA_TYPES) {
            System.out.println("  " + lineCount(dataFilePath(dataType)) + "\t" + dataFilePath(dataType));
        }
    }

    /**
     * Returns the path to the file containing data of the given type.
     * 
     * @param dataType the data type
     * @return the path to the corresponding data file
     */
    private Path dataFilePath(String dataType) {
        return directory.resolve(dataType + ".csv");
    }

    /**
     * Fail validation for the given reason.
     *
     * @param reason
     */
    private void fail(String reason) {
        System.err.println("FAIL: " + reason);
        System.exit(FAILURE_CODE);
    }

    /**
     * Count the number of lines in the file at the given path.
     * 
     * @param path the path to the file
     * @return the number of lines
     * @throws IOException
     */
    private int lineCount(Path path) throws IOException {
        InputStream stream = new BufferedInputStream(new FileInputStream(path.toString()));
        try {
            byte[] c      = new byte[1024];
            int count     = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = stream.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            stream.close();
        }
    }
}
