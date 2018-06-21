package kz.bsbnb.usci.core.repository;

import kz.bsbnb.usci.core.dao.MetaClassDao;
import kz.bsbnb.usci.model.eav.meta.MetaClass;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Baurzhan Makhambetov
 */

@Repository
public class MetaClassRepositoryImpl implements MetaClassRepository, InitializingBean {
    private final MetaClassDao metaClassDao;
    private final HashMap<String, MetaClass> cache = new HashMap<>();
    private final HashMap<Long, String> names = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Autowired
    public MetaClassRepositoryImpl(MetaClassDao metaClassDao) {
        this.metaClassDao = metaClassDao;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //TODO: в режиме разработки очень сильно тормозит
        // TODO: включать только на production
        if (true) return;

        lock.readLock().lock();
        List<MetaClass> metaClassList;
        try {
            metaClassList = metaClassDao.loadAll();
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            for (MetaClass tmpMeta : metaClassList) {
                cache.put(tmpMeta.getClassName(), tmpMeta);
                names.put(tmpMeta.getId(), tmpMeta.getClassName());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public MetaClass getMetaClass(String className) {
        lock.readLock().lock();
        MetaClass metaClass;
        try {
            metaClass = cache.get(className);
        } finally {
            lock.readLock().unlock();
        }

        if (metaClass == null) {
            lock.readLock().lock();
            try {
                metaClass = metaClassDao.load(className);
            } finally {
                lock.readLock().unlock();
            }

            if (metaClass != null) {
                lock.writeLock().lock();
                try {
                    cache.put(className, metaClass);
                    names.put(metaClass.getId(), className);
                } finally {
                    lock.writeLock().unlock();
                }
            }
        }

        return metaClass;
    }

    @Override
    public MetaClass getMetaClass(long id) {
        String className;
        lock.readLock().lock();
        try {
            className = names.get(id);
        } finally {
            lock.readLock().unlock();
        }

        MetaClass metaClass = null;

        if (className != null) {
            lock.readLock().lock();
            try {
                metaClass = cache.get(className);
            } finally {
                lock.readLock().unlock();
            }
        }

        if (metaClass == null) {
            lock.readLock().lock();
            try {
                metaClass = metaClassDao.load(id);
            } finally {
                lock.readLock().unlock();
            }

            if (metaClass != null) {
                lock.writeLock().lock();
                try {
                    cache.put(className, metaClass);
                    names.put(metaClass.getId(), metaClass.getClassName());
                } finally {
                    lock.writeLock().unlock();
                }
            }
        }

        return metaClass;
    }

    @Override
    public List<MetaClass> getMetaClasses() {
        List<MetaClass> metaClassList = new ArrayList<>();
        lock.readLock().lock();
        try {
            for (Map.Entry<String, MetaClass> entry : cache.entrySet())
                metaClassList.add(entry.getValue());
        } finally {
            lock.readLock().unlock();
        }
        return metaClassList;
    }

    @Override
    public void resetCache() {
        lock.writeLock().lock();
        try {
            cache.clear();
            names.clear();
        } finally {
            lock.writeLock().unlock();
        }

        try {
            afterPropertiesSet();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
