package org.damap.base.rest.persons;

/**
 * {@inheritDoc}
 *
 * @deprecated Do not extend this class, provide your own implementation for {@link PersonService}
 *     instead. When using the service as a {@link org.damap.base.integration.PersonService}, use
 *     {@link org.damap.base.integration.mock.MockUniversityPersonServiceImpl} instead.
 */
@Deprecated
public class MockUniversityPersonServiceImpl
    extends org.damap.base.integration.mock.MockUniversityPersonServiceImpl {}
