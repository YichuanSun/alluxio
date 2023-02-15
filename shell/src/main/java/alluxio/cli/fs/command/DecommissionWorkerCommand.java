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
import alluxio.cli.fs.FileSystemShellUtils;
import alluxio.client.block.BlockWorkerInfo;
import alluxio.client.file.FileSystemContext;
import alluxio.exception.AlluxioException;
import alluxio.grpc.DecommissionWorkerPOptions;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Decommission a specific worker, the worker complete decommissioning when
 * all the replicas from them are replicated to other workers. Decommissioned
 * worker are not automatically shutdown and are not chosen for writing new
 * replicas.
 */
public final class DecommissionWorkerCommand extends AbstractFileSystemCommand {

  private static final int DEFAULT_TIMEOUT = 10 * Constants.MINUTE_MS;
  private static final Option TIMEOUT_OPTION =
      Option.builder("t")
          .longOpt("timeout")
          .argName("timeout in milliseconds")
          .numberOfArgs(1)
          .desc("Timeout in milliseconds for decommissioning a"
              + "single worker, default: " + DEFAULT_TIMEOUT)
          .required(false)
          .build();

  private static final Option HOST_OPTION =
      Option.builder("host")
          .longOpt("host")
          .required(true)  // Host option is mandatory.
          .hasArg(true)
          .numberOfArgs(1)
          .argName("host")
          .desc("A worker host name, which is mandatory.")
          .build();

  private static final Option FORCE_OPTION =
      Option.builder("f")
          .required(false)
          .hasArg(false)
          .desc("Force to decommission a worker.")
          .build();

  /**
   * Constructs a new instance to decommission the given worker from Alluxio.
   * @param fsContext the filesystem of Alluxio
   */
  public DecommissionWorkerCommand(FileSystemContext fsContext) {
    super(fsContext);
  }

  @Override
  public int run(CommandLine cl) throws AlluxioException, IOException {
    int timeoutMS = FileSystemShellUtils.getIntArg(cl, TIMEOUT_OPTION, DEFAULT_TIMEOUT);
    String workerHost = cl.getOptionValue(HOST_OPTION.getLongOpt());

    DecommissionWorkerPOptions options =
            DecommissionWorkerPOptions.newBuilder()
                .setTimeOut(timeoutMS).setForced(cl.hasOption("f")).build();

    List<BlockWorkerInfo> cachedWorkers = mFsContext.getCachedWorkers();

    for (BlockWorkerInfo blockWorkerInfo :cachedWorkers)  {
      if (Objects.equals(blockWorkerInfo.getNetAddress().getHost(), workerHost))  {
        try {
          long start = System.currentTimeMillis();
          mFileSystem.decommissionWorker(blockWorkerInfo.getNetAddress(), options);
          long duration = System.currentTimeMillis() - start;
          System.out.printf("Decommission worker %s success, spend: %dms%n",
              workerHost, duration);
        } catch (InterruptedException ie) {
          System.out.println(ie);
        }
        return 0;
      }
    }

    System.out.println("Target worker is not found in Alluxio, please input another name.");
    return 0;
  }

  @Override
  public String getCommandName() {
    return "decommissionWorker";
  }

  @Override
  public Options getOptions() {
    return new Options().addOption(TIMEOUT_OPTION)
        .addOption(HOST_OPTION).addOption(FORCE_OPTION);
  }

  @Override
  public String getUsage() {
    return "decommissionWorker [-t <max_wait_time>] [-f] --host <worker host>";
  }

  @Override
  public String getDescription() {
    return "Decommission a specific worker synchronously, the worker complete decommissioning "
        + "when all the replicas from them are replicated to other workers. Decommissioned "
        + "worker are not automatically shutdown and are not chosen for writing new replicas."
        + "The decommission worker command will return within a certain timeout no matter the "
        + "process is complete.";
  }
}
