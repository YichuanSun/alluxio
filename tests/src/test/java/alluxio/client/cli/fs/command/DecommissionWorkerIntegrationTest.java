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

package alluxio.client.cli.fs.command;

import alluxio.client.cli.fs.AbstractFileSystemShellTest;
import alluxio.client.file.FileSystemTestUtils;
import alluxio.grpc.WritePType;

import org.junit.Test;

/**
 * Tests for decommissionWorker command.
 */
public class DecommissionWorkerIntegrationTest extends AbstractFileSystemShellTest {

  @Test
  public void readOnlyModeTest() {
    // 1. Formally write a file in a worker.
    // 2. set to be readOnlyMode
    // 3. write to the worker, failed
    // 4. cache new file, failed or to other worker
    String fileName = "/testFile";
    FileSystemTestUtils.createByteFile(sFileSystem, fileName, WritePType.MUST_CACHE, 10);

    sLocalAlluxioCluster.getWorkerProcess();
  }
}
