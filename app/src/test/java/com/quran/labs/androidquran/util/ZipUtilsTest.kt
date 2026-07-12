package com.medoapps.www.onlinequran.util

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipUtilsTest {

  companion object {
    private const val TEST_MAX_UNZIPPED_SIZE = 5 * 1024 * 1024 // 5 mb
    private const val CLI_ROOT_DIRECTORY = "src/test/resources"
  }

  private lateinit var destinationDirectory: String
  private val listener =
    ZipUtils.ZipListener { _: Any?, _: Int, _: Int -> }

  @Before
  fun setup() {
    destinationDirectory = "$CLI_ROOT_DIRECTORY/tmp"
    File(destinationDirectory).mkdirs()
  }

  @After
  fun cleanup() {
    val dir = File(destinationDirectory)
    if (dir.exists()) {
      removeDirectory(dir)
    }
  }

  private fun removeDirectory(file: File) {
    if (file.isDirectory) {
      file.listFiles()?.let { files ->
        for (directoryFile in files) {
          removeDirectory(directoryFile)
        }
      }
    }
    require(file.delete()) { "failed to delete: $file" }
  }

  @Ignore("requires src/test/resources/zip_file_100mb.zip, which is not committed to the repo")
  @Test
  fun testFileWithSmallMaxUnzipSize() {
    // max size is 500mb - make it 5mb for the test
    ZipUtils.MAX_UNZIPPED_SIZE = TEST_MAX_UNZIPPED_SIZE
    var e: RuntimeException? = null
    try {
      // thanks to https://github.com/commonsguy/cwac-security for this zip file
      ZipUtils.unzipFile("$CLI_ROOT_DIRECTORY/zip_file_100mb.zip",
        destinationDirectory, null, listener)
    } catch (ise: IllegalStateException) {
      e = ise
    }

    assertThat(e).isNotNull()
  }

  @Ignore("requires src/test/resources/zip_file_100mb.zip, which is not committed to the repo")
  @Test
  fun testNormalFile() {
    var e: RuntimeException? = null
    try {
      // thanks to https://github.com/commonsguy/cwac-security for this zip file
      ZipUtils.unzipFile("$CLI_ROOT_DIRECTORY/zip_file_100mb.zip",
        destinationDirectory, null, listener)
    } catch (ise: IllegalStateException) {
      e = ise
    }

    assertThat(e).isNull()
  }

  @Test
  fun testNestedFileWithoutDirectoryEntry() {
    // Reproduces the print-set download bug: a zip that carries a nested file
    // ("databases/ayahinfo_1000.db") but NO explicit "databases/" directory
    // entry. Extraction must create the parent folder on the fly, otherwise
    // opening the output file fails and unzipFile returns false.
    ZipUtils.MAX_UNZIPPED_SIZE = 0x1f400000 // restore default (other tests shrink it)
    // self-contained sandbox (absolute temp paths, independent of the working dir)
    val sandbox = Files.createTempDirectory("ziputils").toFile()
    try {
      val zipFile = File(sandbox, "src.zip")
      ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
        zos.putNextEntry(ZipEntry("databases/ayahinfo_1000.db"))
        zos.write("ayahinfo".toByteArray())
        zos.closeEntry()
      }
      val extractDir = File(sandbox, "out").apply { mkdirs() }

      val result = ZipUtils.unzipFile(zipFile.absolutePath, extractDir.absolutePath, null, listener)

      assertThat(result).isTrue()
      assertThat(File(extractDir, "databases/ayahinfo_1000.db").exists()).isTrue()
    } finally {
      sandbox.deleteRecursively()
    }
  }

  @Ignore("requires src/test/resources/zip_file_writes_outside.zip, which is not committed to the repo")
  @Test
  fun testZipFileWritesOutside() {
    var e: RuntimeException? = null
    try {
      // thanks to https://github.com/commonsguy/cwac-security for this zip file
      ZipUtils.unzipFile("$CLI_ROOT_DIRECTORY/zip_file_writes_outside.zip",
        destinationDirectory, null, listener)
    } catch (ise: IllegalStateException) {
      e = ise
    }

    assertThat(e).isNotNull()
  }
}
