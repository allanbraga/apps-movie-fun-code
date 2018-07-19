/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.moviefun.albums;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

@Repository
public class AlbumsBean {

    @PersistenceContext(unitName = "albums-unit")
    @Qualifier("albumsEntityManager")
    private EntityManager entityManager;


    private final TransactionTemplate transactionTemplate;

    public AlbumsBean(@Qualifier("transactionManagerAlbums") PlatformTransactionManager transactionManager){

        this.transactionTemplate = new TransactionTemplate(transactionManager);

    }

    public void addAlbum(Album album) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                entityManager.persist(album);
            }
        });

    }

    public List<Album> getAlbums() {
        CriteriaQuery<Album> cq = entityManager.getCriteriaBuilder().createQuery(Album.class);
        cq.select(cq.from(Album.class));
        return entityManager.createQuery(cq).getResultList();
    }
}
