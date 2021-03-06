/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.config.xml.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.ConfigDescription
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterGroup
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.SyntheticBundleInstaller
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.Bundle

/**
 * The ConfigDescriptionsTest is a test for loading of configuration description from XML documents.
 * 
 * @author Alex Tugarev - Initial contribution; Extended tests for options and filters
 *
 */
class ConfigDescriptionsTest extends OSGiTest {

    static final String TEST_BUNDLE_NAME = "ConfigDescriptionsTest.bundle"
	
	ConfigDescriptionRegistry configDescriptionRegistry

	@Before
	void setUp() {
		configDescriptionRegistry = getService(ConfigDescriptionRegistry)
		assertThat configDescriptionRegistry, is(notNullValue())
	}
    
    @After
    void tearDown() {
        SyntheticBundleInstaller.uninstall(getBundleContext(), TEST_BUNDLE_NAME)
    }
	
	@Test
	void 'assert that ConfigDescriptions were loaded properly'() {
		def bundleContext = getBundleContext()
        def initialNumberOfConfigDescriptions = configDescriptionRegistry.getConfigDescriptions().size()
		
        // install test bundle
		Bundle bundle = SyntheticBundleInstaller.install(bundleContext, TEST_BUNDLE_NAME)
		assertThat bundle, is(notNullValue())
		
        def configDescriptions = configDescriptionRegistry.getConfigDescriptions()
        assertThat configDescriptions.size(), is(initialNumberOfConfigDescriptions + 1)
        
        ConfigDescription dummyConfigDescription = configDescriptions.find {
                it.uri.equals(new URI("config:dummyConfig")) }
        assertThat dummyConfigDescription, is(notNullValue())

        def parameters = dummyConfigDescription.parameters
        assertThat parameters.size(), is(6)
        
        ConfigDescriptionParameter ipParameter = parameters.find { it.name.equals("ip") }
        assertThat ipParameter, is(notNullValue())
        assertThat ipParameter.type, is(Type.TEXT)
        ipParameter.with {
	        assertThat groupName, is(null)
            assertThat context, is("network-address")
            assertThat label, is("Network Address")
            assertThat description, is("Network address of the hue bridge.")
            assertThat pattern, is("[0-9]{3}.[0-9]{3}.[0-9]{3}.[0-9]{3}")
            assertThat required, is(true)
            assertThat multiple, is(false)
            assertThat readOnly, is(true)
        }
        
        ConfigDescriptionParameter usernameParameter = parameters.find { it.name.equals("username") }
        assertThat usernameParameter, is(notNullValue())
        assertThat usernameParameter.type, is(Type.TEXT)
        usernameParameter.with {
	        assertThat groupName, is("user")
            assertThat context, is("password")
            assertThat label, is("Username")
            assertThat required, is(false)
            assertThat multiple, is(false)
            assertThat readOnly, is(false)
            assertThat description, is("Name of a registered hue bridge user, that allows to access the API.")
        }
        
        ConfigDescriptionParameter userPassParameter = parameters.find { it.name.equals("user-pass") }
        assertThat userPassParameter, is(notNullValue())
        assertThat userPassParameter.type, is(Type.TEXT)
        userPassParameter.with {
            assertThat min, is(8 as BigDecimal)
            assertThat max, is(16 as BigDecimal)
            assertThat required, is(true)
            assertThat multiple, is(false)
            assertThat readOnly, is(false)
            assertThat context, is("password")
            assertThat label, is("Password")
        }

        ConfigDescriptionParameter colorItemParameter = parameters.find { it.name.equals("color-alarming-light") }
        assertThat colorItemParameter, is(notNullValue())
        assertThat colorItemParameter.type, is(Type.TEXT)
        colorItemParameter.with {
            assertThat required, is(false)
            assertThat readOnly, is(false)
            assertThat context, is("item")
            assertThat filterCriteria, is(notNullValue())
            assertThat filterCriteria.join(", "), is("FilterCriteria [name=\"tags\", value=\"alarm, light\"], FilterCriteria [name=\"type\", value=\"color\"], FilterCriteria [name=\"binding-id\", value=\"hue\"]")
        }

        ConfigDescriptionParameter listParameter1 = parameters.find { it.name.equals("list1") }
        assertThat listParameter1, is(notNullValue())
        assertThat listParameter1.type, is(Type.TEXT)
        listParameter1.with {
            assertThat required, is(false)
            assertThat multiple, is(true)
            assertThat readOnly, is(false)
            assertThat min, is(2 as BigDecimal)
            assertThat max, is(3 as BigDecimal)
            assertThat options, is(notNullValue())
            assertThat advanced, is(false)
            assertThat limitToOptions, is(true)
            assertThat multipleLimit, is(null)
            assertThat options.join(", "), is("ParameterOption [value=\"key1\", label=\"label1\"], ParameterOption [value=\"key2\", label=\"label2\"]")
        }
        
        ConfigDescriptionParameter listParameter2 = parameters.find { it.name.equals("list2") }
        assertThat listParameter2, is(notNullValue())
        assertThat listParameter2.type, is(Type.TEXT)
        listParameter2.with {
            assertThat required, is(false)
            assertThat multiple, is(true)
            assertThat readOnly, is(false)
            assertThat options, is(notNullValue())
            assertThat advanced, is(true)
            assertThat limitToOptions, is(false)
            assertThat multipleLimit, is(4)
        }
        
        def groups = dummyConfigDescription.parameterGroups
        assertThat groups.size(), is(2)

        ConfigDescriptionParameterGroup group1 = groups.find { it.name.equals("group1") }
        assertThat group1, is(notNullValue())
        group1.with {
            assertThat label, is("Group 1")
            assertThat description, is("Description Group 1")
            assertThat advanced, is(false)
            assertThat context, is("Context-Group1")
        }

        ConfigDescriptionParameterGroup group2 = groups.find { it.name.equals("group2") }
        assertThat group1, is(notNullValue())
        group2.with {
            assertThat label, is("Group 2")
            assertThat description, is("Description Group 2")
            assertThat advanced, is(true)
            assertThat context, is("Context-Group2")
        }

        // uninstall test bundle
        bundle.uninstall();
        assertThat bundle.state, is(Bundle.UNINSTALLED)
	}
}
