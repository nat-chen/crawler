package com.github.natchen;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MockDataGenerator {
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    private static void mockData(SqlSessionFactory sqlSessionFactory, int howMany) {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            List<News> currentNews = session.selectList("com.github.natchen.MockMapper.selectNews");
            int count = howMany - currentNews.size();
            Random random = new Random();

            try {
                while (count-- > 0) {
                    int index = random.nextInt(currentNews.size());
                    News newsToBeInserted = new News(currentNews.get(index));

                    Instant currentTime = newsToBeInserted.getCreatedAt();
                    currentTime = currentTime.minusSeconds(random.nextInt(3600 * 24 * 365));
                    newsToBeInserted.setUpdatedAt(currentTime);
                    newsToBeInserted.setCreatedAt(currentTime);

                    session.insert("com.github.natchen.MockMapper.insertNews", newsToBeInserted);

                    System.out.printf("Left " + count);
                    if (count % 2000 == 0) {
                        session.flushStatements();
                    }

                }
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException(e);
            }
        }

    }
}
