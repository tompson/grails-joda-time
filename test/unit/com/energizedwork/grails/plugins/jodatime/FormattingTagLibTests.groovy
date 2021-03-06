/*
 * Copyright 2010 Rob Fletcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.energizedwork.grails.plugins.jodatime

import grails.test.TagLibUnitTestCase
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException
import org.joda.time.chrono.IslamicChronology
import org.joda.time.format.DateTimeFormat
import org.junit.After
import org.junit.Before
import org.junit.Test
import static org.hamcrest.CoreMatchers.equalTo
import org.joda.time.*
import static org.junit.Assert.assertThat

class FormattingTagLibTests extends TagLibUnitTestCase {

	@Before
	void setUp() {
		super.setUp()

		mockConfig "jodatime {}"

		tagLib.metaClass.getOutput = {-> delegate.out.toString() }

		def fixedDateTime = new DateTime(2008, 10, 2, 2, 50, 33, 0)
		DateTimeUtils.setCurrentMillisFixed fixedDateTime.getMillis()

		tagLib.request.addPreferredLocale Locale.UK
	}

	@After
	void tearDown() {
		super.tearDown()
		DateTimeUtils.setCurrentMillisSystem()
	}

	@Test
	void formatDefaultsToCurrentDateTimeInRequestLocale() {
		tagLib.format([:])
		assertThat tagLib.output, equalTo("02-Oct-2008 02:50:33")
	}

	@Test
	void formatUsesValueIfSpecified() {
		tagLib.format(value: new DateTime(1971, 11, 29, 16, 22, 0, 0))
		assertThat tagLib.output, equalTo("29-Nov-1971 16:22:00")
	}

	@Test
	void formatUsesLocaleIfSpecified() {
		tagLib.format(locale: Locale.FRANCE)
		assertEquals("2 oct. 2008 02:50:33", tagLib.output)
	}

	@Test
	void formatUsesZoneIfSpecified() {
		JodaTimeUtils.withDateTimeZone(DateTimeZone.forID("Europe/London")) {
			tagLib.format(zone: DateTimeZone.forID("America/Vancouver"))
		}
		assertThat tagLib.output, equalTo("01-Oct-2008 18:50:33")
	}

	@Test
	void formatUsesChronologyIfSpecified() {
		tagLib.format(chronology: IslamicChronology.instance)
		assertThat tagLib.output, equalTo("01-10-1429 02:50:33")
	}

	@Test
	void formatUsesStyleIfSpecified() {
		JodaTimeUtils.withDateTimeZone(DateTimeZone.forID('Europe/London')) {
			tagLib.format(style: "LL")
		}
		assertThat tagLib.output, equalTo("02 October 2008 02:50:33 BST")
	}

	@Test
	void formatStyleAttributeInPreferenceToConfigDefault() {
		mockConfig '''
			jodatime.format.org.joda.time.DateTime="[z] dd-MM-yyyy@HH:mm:ss"
		'''
		JodaTimeUtils.withDateTimeZone(DateTimeZone.forID('Europe/London')) {
			tagLib.format(style: "F-")
		}
		assertThat tagLib.output, equalTo("Thursday, 2 October 2008")
	}

	@Test
	void formatAcceptsLocalDateValue() {
		tagLib.format(value: new LocalDate(1985, 10, 13))
		assertThat tagLib.output, equalTo("13-Oct-1985")
	}

	@Test
	void formatAcceptsLocalTimeValue() {
		tagLib.format(value: new LocalTime(16, 23, 42))
		assertThat tagLib.output, equalTo("16:23:42")
	}

	@Test(expected = GrailsTagException)
	void formatDoesNotAcceptBothStyleAndPattern() {
		tagLib.format(style: "SS", pattern: "yyyy-MM-dd HH:mm:ss")
	}

	@Test
	void formatUsesPatternIfSpecified() {
		JodaTimeUtils.withDateTimeZone(DateTimeZone.forID("Europe/London")) {
			tagLib.format(pattern: "[z] dd-MM-yyyy@HH:mm:ss")
		}
		assertThat tagLib.output, equalTo("[BST] 02-10-2008@02:50:33")
	}

	@Test
	void formatUsesPatternIfConfigured() {
		mockConfig '''
			jodatime.format.org.joda.time.DateTime="[z] dd-MM-yyyy@HH:mm:ss"
		'''
		JodaTimeUtils.withDateTimeZone(DateTimeZone.forID("Europe/London")) {
			tagLib.format([:])
		}
		assertThat tagLib.output, equalTo("[BST] 02-10-2008@02:50:33")
	}

	@Test
	void patternDefaultsToDateTime() {
		tagLib.inputPattern([:])
		assertThat tagLib.output, equalTo(DateTimeFormat.patternForStyle("SS", Locale.UK))
	}

	@Test
	void patternAcceptsTypeAttribute() {
		tagLib.inputPattern(type: LocalDate)
		assertThat tagLib.output, equalTo(DateTimeFormat.patternForStyle("S-", Locale.UK))
	}

	@Test
	void patternAcceptsTypeAsString() {
		tagLib.inputPattern(type: LocalDate.name)
		assertThat tagLib.output, equalTo(DateTimeFormat.patternForStyle("S-", Locale.UK))
	}

	@Test
	void patternAcceptsLocaleAttribute() {
		tagLib.inputPattern(locale: Locale.CANADA_FRENCH)
		assertThat tagLib.output, equalTo(DateTimeFormat.patternForStyle("SS", Locale.CANADA_FRENCH))
	}

	@Test
	void patternAcceptsLocaleAsString() {
		tagLib.inputPattern(locale: "fr_CA")
		assertThat tagLib.output, equalTo(DateTimeFormat.patternForStyle("SS", Locale.CANADA_FRENCH))
	}

	@Test
	void patternDisplaysConfiguredPatternIfSet() {
		mockConfig '''
			jodatime.format.org.joda.time.DateTime="[z] dd-MM-yyyy@HH:mm:ss"
		'''
		tagLib.inputPattern([:])
		assertThat tagLib.output, equalTo("[z] dd-MM-yyyy@HH:mm:ss")
	}

}