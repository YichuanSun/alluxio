/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package alluxio.cli.fs.command;

import alluxio.Constants;
import alluxio.annotation.PublicApi;
import alluxio.cli.fs.FileSystemShellUtils;
import alluxio.cli.fs.command.AbstractFileSystemCommand;
import alluxio.client.block.BlockWorkerInfo;
import alluxio.client.file.FileSystemContext;
import alluxio.exception.AlluxioException;
import alluxio.wire.WorkerNetAddress;
import alluxio.worker.block.BlockWorker;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import alluxio.grpc.FreeWorkerPOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Frees all blocks of given worker(s) synchronously from Alluxio cluster.
 * TODO(Yichuan Sun):
 *  1. -t handler, set default value.
 *  2. -f handler, set default to false.
 *  3. runWildCardCmd?
 *  4.
 */

@PublicApi
public final class FreeWorkerCommand extends AbstractFileSystemCommand {

    private static final Option FORCE_OPTION =
            Option.builder("f")
                    .required(false)
                    .hasArg(false)
                    .desc("force to free all blocks of given worker(s) even pinned")
                    .build();

    private static final int DEFAULT_PARALLELISM = 1;

    private static final Option PARALLELISM_OPTION =
            Option.builder("p")
                    .longOpt("parallelism")
                    .argName("# concurrent operations")
                    .numberOfArgs(1)
                    .desc("Number of concurrent persist operations, default: " + DEFAULT_PARALLELISM)
                    .required(false)
                    .build();

    private static final int DEFAULT_TIMEOUT = 2 * Constants.MINUTE_MS;

    private static final Option TIMEOUT_OPTION =
            Option.builder("t")
                    .longOpt("timeout")
                    .argName("timeout in milliseconds")
                    .numberOfArgs(1)
                    .desc("Time in milliseconds for freeing a single worker to time out; default:"
                            + DEFAULT_TIMEOUT)
                    .required(false)
                    .build();

    private static final String DEFAULT_WORKER_NAME = "";

    private static final Option HOSTS_OPTION =
            Option.builder("h")
                    .longOpt("hosts")
                    .required(true)         // Host option is mandatory.
                    .hasArg(true)
                    .numberOfArgs(1)
                    .argName("hosts")
                    .desc("A list of worker hosts separated by comma."
                            + " When host and locality options are not set,"
                            + " all hosts will be selected unless explicitly excluded by setting excluded option"
                            + "('excluded-hosts', 'excluded-host-file', 'excluded-locality'"
                            + " and 'excluded-locality-file')."
                            + " Only one of the 'hosts' and 'host-file' should be set,"
                            + " and it should not be set with excluded option together.")
                    .build();


    /**
     *
     * Constructs a new instance to free the given worker(s) from Alluxio.
     *
     * @param fsContext fs command context
     */
    public FreeWorkerCommand(FileSystemContext fsContext) {
        super(fsContext);
    }

    public int run(CommandLine cl) throws AlluxioException, IOException {
        int parallelism = FileSystemShellUtils.getIntArg(cl, PARALLELISM_OPTION, DEFAULT_PARALLELISM);
        int timeoutMs = (int) FileSystemShellUtils.getMsArg(cl, TIMEOUT_OPTION, DEFAULT_TIMEOUT);

        String workerName = FileSystemShellUtils.getWorkerNameArg(cl, HOSTS_OPTION, DEFAULT_WORKER_NAME);

        // Not sure correct.
        // The TimeOut is not consistent. int64, int, and long.
        FreeWorkerPOptions options =
                FreeWorkerPOptions.newBuilder().setForced(cl.hasOption("f")).setTimeOut(timeoutMs).build();

        if (parallelism > 1) {
            System.out.println("FreeWorker command only support free a worker at a time currently.");
            return 0;
        }

/*
        Test for timeoutMs and workerName. print is correct.
        System.out.println(timeoutMs);
        System.out.println(workerName);
        List<WorkerNetAddress> allWorkers = mFsContext;
*/
        // Test allWorkerNetAddresses

        List<BlockWorkerInfo> cachedWorkers = mFsContext.getCachedWorkers();

        boolean flag = false;

        // Only Support free one Worker.
        WorkerNetAddress targetWorkerNetAddress = new WorkerNetAddress();
        for (BlockWorkerInfo blockWorkerInfo : cachedWorkers) {
            System.out.println("---------------------------------------------------------------------------");
            System.out.println(blockWorkerInfo.getNetAddress().getHost());
            System.out.println("---------------------------------------------------------------------------");
            if (Objects.equals(blockWorkerInfo.getNetAddress().getHost(), workerName))  {
                System.out.println("to_String:\t" + blockWorkerInfo.getNetAddress().toString());
                targetWorkerNetAddress = blockWorkerInfo.getNetAddress();
                flag = true;
                break;
            }
        }

        // exception or return ?
        System.out.println("---------------------------------------------------------------------------");
        if (!flag)   {
            System.out.println("Target Worker is not found in Alluxio, please input another name.");
            return 0;
        }
        System.out.println("Target Worker is valid, next step is sending workerNetAddress to master.");

        // TODO(Yichuan Sun): Add timeout handler.

        // Now targetWorkerNetAddress is represented a valid worker in Alluxio.
        mFileSystem.freeWorker(targetWorkerNetAddress, options);
        return 0;
    }

    @Override
    public String getCommandName() {
        return "freeWorker";
    }

    @Override
    public Options getOptions() {
        return new Options().addOption(FORCE_OPTION)
                .addOption(PARALLELISM_OPTION)
                .addOption(TIMEOUT_OPTION)
                .addOption(HOSTS_OPTION);
    }

    public String getUsage() {
        return "freeWorker [-f] [-t max_wait_time] <List<worker>>";
    }

    @Override
    public String getDescription() {
        return "Frees all the blocks synchronously of specific worker(s) in Alluxio."
                + " Specify -f to force freeing pinned files in the directory."
                + " Specify -t to set a maximum wait time.";
    }

}
