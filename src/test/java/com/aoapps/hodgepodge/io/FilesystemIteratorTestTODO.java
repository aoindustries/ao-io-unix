/*
 * ao-io-posix - Java interface to native POSIX filesystem objects.
 * Copyright (C) 2008, 2009, 2010, 2011, 2013, 2015, 2016, 2017, 2019, 2020, 2021  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-io-posix.
 *
 * ao-io-posix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-io-posix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-io-posix.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoapps.hodgepodge.io;

import com.aoapps.io.posix.PosixFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the filesystem iterator.
 *
 * TODO: This test requires libaocode.so, find a way to compile this and
 *       include in the LD_LIBRARY_PATH of the test runner.
 *
 * @author  AO Industries, Inc.
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class FilesystemIteratorTestTODO extends TestCase {

	public FilesystemIteratorTestTODO(String testName) {
		super(testName);
	}

	private PosixFile tempDir;

	@Override
	protected void setUp() throws Exception {
		tempDir = new PosixFile(Files.createTempDirectory("FilesystemIteratorTest.").toFile());
		PosixFile tmp = new PosixFile(tempDir, "tmp", true).mkdir(false, 0755);
		PosixFile tmp_something = new PosixFile(tmp, "something", true);
		new FileOutputStream(tmp_something.getFile()).close();
		tmp_something.setMode(0644);
		PosixFile home = new PosixFile(tempDir, "home", true).mkdir(false, 0755);
		PosixFile home_a = new PosixFile(home, "a", true).mkdir(false, 0755);
		PosixFile home_a_aoadmin = new PosixFile(home_a, "aoadmin", true).mkdir(false, 0700);
		PosixFile home_a_aoadmin_something = new PosixFile(home_a_aoadmin, "something", true);
		new FileOutputStream(home_a_aoadmin_something.getFile()).close();
		home_a_aoadmin_something.setMode(0600);
		PosixFile home_a_aoadmin_something2 = new PosixFile(home_a_aoadmin, "something2", true);
		new FileOutputStream(home_a_aoadmin_something2.getFile()).close();
		home_a_aoadmin_something2.setMode(0644);
		PosixFile home_a_aoadmin_badlink = new PosixFile(home_a_aoadmin, "badlink", true);
		home_a_aoadmin_badlink.symLink("../aoadmin");
		PosixFile home_a_aoadmin_brokenlink = new PosixFile(home_a_aoadmin, "brokenlink", true);
		home_a_aoadmin_brokenlink.symLink("brokenlinknotarget");
	}

	@Override
	protected void tearDown() throws Exception {
		tempDir.deleteRecursive();
		tempDir = null;
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(FilesystemIteratorTestTODO.class);
		return suite;
	}

	/**
	 * Without any rules the iterator should not return anything (defaults to exclude all).
	 */
	public void testIteratorNone() throws IOException {
		Map<String, FilesystemIteratorRule> rules = Collections.emptyMap();
		Map<String, FilesystemIteratorRule> prefixRules = Collections.emptyMap();
		List<String> expectedResults = new ArrayList<>();
		doTest(
			rules,
			prefixRules,
			expectedResults
		);
	}

	public void testIterateAll() throws IOException {
		Map<String, FilesystemIteratorRule> rules = Collections.singletonMap(tempDir.getPath(), FilesystemIteratorRule.OK);
		Map<String, FilesystemIteratorRule> prefixRules = Collections.emptyMap();
		List<String> expectedResults = new ArrayList<>();
		expectedResults.add("/");
		expectedResults.add("/tmp");
		expectedResults.add(tempDir.getPath());
		expectedResults.add(tempDir.getPath()+"/home");
		expectedResults.add(tempDir.getPath()+"/home/a");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin/badlink");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin/brokenlink");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin/something");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin/something2");
		expectedResults.add(tempDir.getPath()+"/tmp");
		expectedResults.add(tempDir.getPath()+"/tmp/something");
		doTest(
			rules,
			prefixRules,
			expectedResults
		);
	}

	public void testIncludeDirectoryAndSkipContents() throws IOException {
		Map<String, FilesystemIteratorRule> rules = new HashMap<>();
		rules.put(tempDir.getPath(), FilesystemIteratorRule.OK);
		rules.put(tempDir.getPath()+"/tmp/", FilesystemIteratorRule.SKIP);
		Map<String, FilesystemIteratorRule> prefixRules = Collections.emptyMap();
		List<String> expectedResults = new ArrayList<>();
		expectedResults.add("/");
		expectedResults.add("/tmp");
		expectedResults.add(tempDir.getPath());
		expectedResults.add(tempDir.getPath()+"/home");
		expectedResults.add(tempDir.getPath()+"/home/a");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin/badlink");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin/brokenlink");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin/something");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin/something2");
		expectedResults.add(tempDir.getPath()+"/tmp");
		doTest(
			rules,
			prefixRules,
			expectedResults
		);
	}

	public void testFileExistsRuleExists() throws IOException {
		Map<String, FilesystemIteratorRule> rules = new HashMap<>();
		rules.put(tempDir.getPath(), FilesystemIteratorRule.OK);
		rules.put(
			tempDir.getPath()+"/home/a/aoadmin/something",
			new FileExistsRule(new String[] {tempDir.getPath()+"/home/a/aoadmin/something2"}, FilesystemIteratorRule.SKIP, FilesystemIteratorRule.OK)
		);
		Map<String, FilesystemIteratorRule> prefixRules = Collections.emptyMap();
		List<String> expectedResults = new ArrayList<>();
		expectedResults.add("/");
		expectedResults.add("/tmp");
		expectedResults.add(tempDir.getPath());
		expectedResults.add(tempDir.getPath()+"/home");
		expectedResults.add(tempDir.getPath()+"/home/a");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin/badlink");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin/brokenlink");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin/something2");
		expectedResults.add(tempDir.getPath()+"/tmp");
		expectedResults.add(tempDir.getPath()+"/tmp/something");
		doTest(
			rules,
			prefixRules,
			expectedResults
		);
	}

	public void testFileExistsRuleNotExists() throws IOException {
		Map<String, FilesystemIteratorRule> rules = new HashMap<>();
		rules.put(tempDir.getPath(), FilesystemIteratorRule.OK);
		rules.put(
			tempDir.getPath()+"/home/a/aoadmin/something",
			new FileExistsRule(new String[] {tempDir.getPath()+"/home/a/aoadmin/somethingNotHere"}, FilesystemIteratorRule.SKIP, FilesystemIteratorRule.OK)
		);
		Map<String, FilesystemIteratorRule> prefixRules = Collections.emptyMap();
		List<String> expectedResults = new ArrayList<>();
		expectedResults.add("/");
		expectedResults.add("/tmp");
		expectedResults.add(tempDir.getPath());
		expectedResults.add(tempDir.getPath()+"/home");
		expectedResults.add(tempDir.getPath()+"/home/a");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin/badlink");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin/brokenlink");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin/something");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin/something2");
		expectedResults.add(tempDir.getPath()+"/tmp");
		expectedResults.add(tempDir.getPath()+"/tmp/something");
		doTest(
			rules,
			prefixRules,
			expectedResults
		);
	}

	public void testFileExistsRuleBrokenLink() throws IOException {
		Map<String, FilesystemIteratorRule> rules = new HashMap<>();
		rules.put(tempDir.getPath(), FilesystemIteratorRule.OK);
		rules.put(
			tempDir.getPath()+"/home/a/aoadmin/something",
			new FileExistsRule(new String[] {tempDir.getPath()+"/home/a/aoadmin/brokenlink"}, FilesystemIteratorRule.SKIP, FilesystemIteratorRule.OK)
		);
		Map<String, FilesystemIteratorRule> prefixRules = Collections.emptyMap();
		List<String> expectedResults = new ArrayList<>();
		expectedResults.add("/");
		expectedResults.add("/tmp");
		expectedResults.add(tempDir.getPath());
		expectedResults.add(tempDir.getPath()+"/home");
		expectedResults.add(tempDir.getPath()+"/home/a");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin/badlink");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin/brokenlink");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin/something");
		expectedResults.add(tempDir.getPath()+"/home/a/aoadmin/something2");
		expectedResults.add(tempDir.getPath()+"/tmp");
		expectedResults.add(tempDir.getPath()+"/tmp/something");
		doTest(
			rules,
			prefixRules,
			expectedResults
		);
	}

	/**
	 * Performs the test against expected results.
	 */
	private void doTest(
		Map<String, FilesystemIteratorRule> rules,
		Map<String, FilesystemIteratorRule> prefixRules,
		List<String> expectedResults
	) throws IOException {
		FilesystemIterator iterator = new FilesystemIterator(
			rules,
			prefixRules
		);
		List<String> results = new ArrayList<>();
		File file;
		while((file=iterator.getNextFile())!=null) {
			results.add(file.getPath());
		}
		int widestExpected = 8;
		for(String S : expectedResults) {
			int len = S.length();
			if(len>widestExpected) widestExpected = len;
		}
		int widestActual = 6;
		for(String S : results) {
			int len = S.length();
			if(len>widestActual) widestActual = len;
		}
		int longerList = Math.max(expectedResults.size(), results.size());
		System.out.print("Expected");
		for(int d=8;d<widestExpected;d++) {
			System.out.print(' ');
		}
		System.out.print(' ');
		System.out.print("Actual");
		for(int d=6;d<widestActual;d++) {
			System.out.print(' ');
		}
		System.out.println();
		for(int c=0;c<longerList;c++) {
			String expected = c<expectedResults.size() ? expectedResults.get(c) : "";
			System.out.print(expected);
			for(int d=expected.length();d<widestExpected;d++) {
				System.out.print(' ');
			}
			System.out.print(' ');
			String actual = c<results.size() ? results.get(c) : "";
			System.out.print(actual);
			for(int d=actual.length();d<widestActual;d++) {
				System.out.print(' ');
			}
			System.out.println();
		}
		assertEquals("Results are not as expected", expectedResults, results);
	}
}
