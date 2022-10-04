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
import alluxio.client.block.BlockWorkerInfo;
import alluxio.client.file.FileSystemContext;
import alluxio.exception.AlluxioException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import alluxio.grpc.FreeWorkerPOptions;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Frees all blocks of given worker(s) synchronously from Alluxio cluster.
 */

@PublicApi
public final class FreeWorkerCommand extends AbstractFileSystemCommand {

  private static final int DEFAULT_PARALLELISM = 1;

  private static final String DEFAULT_WORKER_NAME = "";

  private static final Option HOSTS_OPTION =
          Option.builder("h")
                  .longOpt("hosts")
                  .required(true)         // Host option is mandatory.
                  .hasArg(true)
                  .numberOfArgs(1)
                  .argName("hosts")
                  .desc("A worker host name, which is mandatory.")
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
    String workerName = FileSystemShellUtils.getWorkerNameArg(cl, HOSTS_OPTION, DEFAULT_WORKER_NAME);

    // Not sure.
    // The TimeOut is not consistent. int64, int, and long.
    FreeWorkerPOptions options =
            FreeWorkerPOptions.newBuilder().build();

    // TODO(Tony Sun): Can we just use cached workers?
    List<BlockWorkerInfo> cachedWorkers = mFsContext.getCachedWorkers();

    // Only Support free one Worker.
    for (BlockWorkerInfo blockWorkerInfo : cachedWorkers) {
      if (Objects.equals(blockWorkerInfo.getNetAddress().getHost(), workerName))  {
        // TODO(Tony Sun): Do we need a timeout handler for freeWorker cmd?
        mFileSystem.freeWorker(blockWorkerInfo.getNetAddress(), options);
        return 0;
      }
    }

    // exception or return ?
    System.out.println("Target worker is not found in Alluxio, please input another name.");
    return 0;
  }

  @Override
  public String getCommandName() {
    return "freeWorker";
  }

  @Override
  public Options getOptions() {
    return new Options().addOption(HOSTS_OPTION);
  }

  public String getUsage() {
    return "freeWorker -h $workerName";
  }

  @Override
  public String getDescription() {
    return "Frees all the blocks synchronously of specific worker(s) in Alluxio."
            + " Specify -t to set a maximum wait time.";
  }

}
