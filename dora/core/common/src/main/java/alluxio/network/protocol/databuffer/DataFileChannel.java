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

package alluxio.network.protocol.databuffer;

import com.google.common.base.Preconditions;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * A DataBuffer with the underlying data being a {@link FileChannel}.
 */
public final class DataFileChannel implements DataBuffer {
  private final FileRegion mFileRegion;
  private final long mLength;

  /**
   *
   * @param file The file
   * @param offset The offset into the FileChannel
   * @param length The length of the data to read
   */
  public DataFileChannel(File file, long offset, long length) {
    this(new DefaultFileRegion(Preconditions.checkNotNull(file, "file"), offset, length), length);
  }

  /**
   *
   * @param fileChannel The open file channel
   * @param offset The offset into the FileChannel
   * @param length The length of the data to read
   */
  public DataFileChannel(FileChannel fileChannel, long offset, long length) {
    this(new DefaultFileRegion(Preconditions.checkNotNull(fileChannel, "fileChannel"),
        offset, length), length);
  }

  private DataFileChannel(FileRegion fileRegion, long length) {
    mFileRegion = fileRegion;
    mLength = length;
  }

  @Override
  public Object getNettyOutput() {
    return mFileRegion;
  }

  @Override
  public long getLength() {
    return mLength;
  }

  @Override
  public ByteBuffer getReadOnlyByteBuffer() {
    throw new UnsupportedOperationException(
        "DataFileChannel#getReadOnlyByteBuffer is not implemented.");
  }

  @Override
  public void readBytes(byte[] dst, int dstIndex, int length) {
    throw new UnsupportedOperationException("DataFileChannel#readBytes is not implemented.");
  }

  @Override
  public void readBytes(OutputStream outputStream, int length) throws IOException {
    throw new UnsupportedOperationException("DataFileChannel#readBytes is not implemented.");
  }

  @Override
  public void readBytes(ByteBuffer outputBuf) {
    throw new UnsupportedOperationException("DataFileChannel#readBytes is not implemented.");
  }

  @Override
  public int readableBytes() {
    int lengthInt = (int) mLength;
    Preconditions.checkArgument(mLength == (long) lengthInt,
        "size of data is %s, cannot be cast to int", mLength);
    return lengthInt;
  }

  @Override
  public void release() {
    if (mFileRegion.refCnt() > 0) {
      mFileRegion.release();
    }
  }
}
