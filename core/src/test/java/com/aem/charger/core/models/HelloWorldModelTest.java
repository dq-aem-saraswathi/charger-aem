package com.aem.charger.core.models;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import com.aem.charger.core.testcontext.AppAemContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(AemContextExtension.class)
class HelloWorldModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    private HelloWorldModel hello;

    private Page page;
    private Resource resource;

    @BeforeEach
    public void setup() throws Exception {
        // Register the Sling Model class
        context.addModelsForClasses(HelloWorldModel.class);

        // prepare a page with a test resource
        page = context.create().page("/content/mypage");
        resource = context.create().resource(page, "hello",
                "sling:resourceType", "charger/components/helloworld");

        // create sling model
        hello = resource.adaptTo(HelloWorldModel.class);
    }

    @Test
    void testGetMessage() throws Exception {
        String msg = hello.getMessage();
        assertNotNull(msg);
        assertTrue(StringUtils.contains(msg, resource.getResourceType()));
        assertTrue(StringUtils.contains(msg, page.getPath()));
    }
}
