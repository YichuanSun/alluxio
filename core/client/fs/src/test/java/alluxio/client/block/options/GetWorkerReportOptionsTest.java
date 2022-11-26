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

package alluxio.client.block.options;

import alluxio.grpc.GetWorkerReportPOptions;
import alluxio.grpc.WorkerInfoField;
import alluxio.grpc.WorkerRange;

import org.junit.Assert;
import org.junit.Test;

public class GetWorkerReportOptionsTest {
  /**
   * Check whether WorkerInfoField class and WorkerInfoField in proto file has identical fields.
   */
  @Test
  public void workerInfoFieldMapTest() {
    // If Options has a WorkerInfoField which POptions does not have, throw IOException.
    Assert.assertEquals(GetWorkerReportPOptions.getDefaultInstance(),
            GetWorkerReportOptions.defaults().toProto());
    // If POptions has a WorkerInfoField which Options does not have,
    // the constructive method will fail.
    for (WorkerInfoField field : WorkerInfoField.values())  {
      Assert.assertEquals(field,
              GetWorkerReportOptions.WorkerInfoField.fromProto(field).toProto());
    }
    for (WorkerRange range : WorkerRange.values())  {
      Assert.assertEquals(range, GetWorkerReportOptions.WorkerRange.fromProto(range).toProto());
    }
  }
}
