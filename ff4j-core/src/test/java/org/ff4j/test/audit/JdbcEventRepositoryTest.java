package org.ff4j.test.audit;

import static org.mockito.Mockito.doThrow;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.ff4j.audit.Event;
import org.ff4j.audit.EventType;

/*
 * #%L
 * ff4j-core
 * %%
 * Copyright (C) 2013 - 2015 Ff4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.ff4j.audit.repository.EventRepository;
import org.ff4j.audit.repository.JdbcEventRepository;
import org.ff4j.exception.AuditAccessException;
import org.ff4j.exception.FeatureAccessException;
import org.ff4j.utils.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

/**
 * Unit testing of JDBC implementation of {@link EventRepository}.
 *
 * @author Cedrick Lunven (@clunven)
 */
public class JdbcEventRepositoryTest extends AbstractEventRepositoryTest {
    
    /** DataBase. */
    private EmbeddedDatabase db;

    /** Builder. */
    private EmbeddedDatabaseBuilder builder = null;
    
    /** {@inheritDoc} */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        db = builder.setType(EmbeddedDatabaseType.HSQL).//
                addScript("classpath:schema-ddl.sql").//
                addScript("classpath:ff-store.sql"). //
                build();
    }

    /** {@inheritDoc} */
    @After
    public void tearDown() throws Exception {
        db.shutdown();
    }
    
    /** {@inheritDoc} */
    @Override
    protected EventRepository initRepository() {
        //sqlDataSource = JdbcTestHelper.createInMemoryHQLDataSource();
         builder = new EmbeddedDatabaseBuilder();
         db = builder.setType(EmbeddedDatabaseType.HSQL).//
                 addScript("classpath:schema-ddl.sql").//
                 addScript("classpath:ff-store.sql").//
                 build();
        return new JdbcEventRepository(db);
    }
    
    @Test
    public void testFeatureHitsPie() {
        JdbcEventRepository jrepo = (JdbcEventRepository) repo;
        jrepo.getDataSource();
        jrepo.saveEvent(new Event("aer", EventType.FEATURE_CHECK_ON));
        jrepo.saveEvent(new Event("aer", EventType.FEATURE_CHECK_OFF));
        jrepo.saveEvent(new Event("aer", EventType.ENABLE_FEATURE));
        jrepo.saveEvent(new Event("aer", EventType.DISABLE_FEATURE));
        jrepo.getFeatureHitsPie("aer", (System.currentTimeMillis() - 10000), (System.currentTimeMillis() + 10000));
    }
    
    @Test
    public void testFeatureHitsPie2() {
        JdbcEventRepository jrepo = (JdbcEventRepository) repo;
        jrepo.getDataSource();
        jrepo.saveEvent(new Event("aer", EventType.DISABLE_FEATUREGROUP));
        jrepo.getFeatureHitsPie("aer", (System.currentTimeMillis() - 10000), (System.currentTimeMillis() + 10000));
    }
    
    @Test
    public void testJdbcHItPie() {
        JdbcEventRepository jrepo = (JdbcEventRepository) repo;
        jrepo.getDataSource();
        jrepo.saveEvent(new Event("aer", EventType.FEATURE_CHECK_ON));
        jrepo.saveEvent(new Event("aer", EventType.FEATURE_CHECK_OFF));
        jrepo.saveEvent(new Event("aer", EventType.ENABLE_FEATURE));
        jrepo.getHitsPieChart((System.currentTimeMillis() - 10000), (System.currentTimeMillis() + 10000));
        jrepo.setDataSource(null); 
    }
    
    @Test(expected = AuditAccessException.class)
    public void testJdbcSpec2() throws SQLException {
        JdbcEventRepository jrepo = (JdbcEventRepository) repo;
        DataSource mockDS = Mockito.mock(DataSource.class);
        doThrow(new SQLException()).when(mockDS).getConnection();
        jrepo.setDataSource(mockDS);
        jrepo.getTotalEventCount();
    }
    
    @Test(expected = AuditAccessException.class)
    public void testJdbcSaveEventKO()  throws SQLException {
        JdbcEventRepository jrepo = (JdbcEventRepository) repo;
        DataSource mockDS = Mockito.mock(DataSource.class);
        doThrow(new SQLException()).when(mockDS).getConnection();
        jrepo.setDataSource(mockDS);
        jrepo.saveEvent(new Event("ee", EventType.CREATE_FEATURE));
    }
    
    @Test(expected = FeatureAccessException.class)
    public void testJdbcFeatureNamesKO()  throws SQLException {
        JdbcEventRepository jrepo = (JdbcEventRepository) repo;
        DataSource mockDS = Mockito.mock(DataSource.class);
        doThrow(new SQLException()).when(mockDS).getConnection();
        jrepo.setDataSource(mockDS);
        jrepo.getFeatureNames();
    }
    
    @Test(expected = FeatureAccessException.class)
    public void testJdbcHitPieCharts()  throws SQLException {
        JdbcEventRepository jrepo = (JdbcEventRepository) repo;
        DataSource mockDS = Mockito.mock(DataSource.class);
        doThrow(new SQLException()).when(mockDS).getConnection();
        jrepo.setDataSource(mockDS);
        jrepo.getHitsPieChart(0, 1);
    }
    
    @Test(expected = AuditAccessException.class)
    public void testJdbcHitBarCharts()  throws SQLException {
        JdbcEventRepository jrepo = (JdbcEventRepository) repo;
        DataSource mockDS = Mockito.mock(DataSource.class);
        doThrow(new SQLException()).when(mockDS).getConnection();
        jrepo.setDataSource(mockDS);
        jrepo.getHitsBarChart(Util.set("1"), 0, 1, 2);
    }
    
    @Test(expected = AuditAccessException.class)
    public void testgetFeatureHitsPieKo()  throws SQLException {
        JdbcEventRepository jrepo = (JdbcEventRepository) repo;
        DataSource mockDS = Mockito.mock(DataSource.class);
        doThrow(new SQLException()).when(mockDS).getConnection();
        jrepo.setDataSource(mockDS);
        jrepo.getFeatureHitsPie("f1", 0, 1);
    }

}
//