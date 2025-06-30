package org.damap.base.integration.pure;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * This mock only exists to work around CDI's convoluted mess and make sure not only the
 * MockProjectService gets loaded.
 */
@Priority(1)
@ApplicationScoped
public class PureProjectServiceMock extends PureProjectService {}
