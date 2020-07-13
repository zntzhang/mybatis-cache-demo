package mapper;

import entity.StudentEntity;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class StudentMapperTest {

    private SqlSessionFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config.xml"));

    }

    @Test
    public void showDefaultCacheConfiguration() {
        System.out.println("本地缓存范围: " + factory.getConfiguration().getLocalCacheScope());
        System.out.println("二级缓存是否被启用: " + factory.getConfiguration().isCacheEnabled());
    }

    /**
     *   <setting name="localCacheScope" value="SESSION"/>
     *   <setting name="cacheEnabled" value="true"/>
     * @throws Exception
     */
    @Test
    public void testLocalCache() throws Exception {
        SqlSession sqlSession = factory.openSession(true);// true为自动提交事务
        StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);
        //第一次经过数据库查询
        System.out.println(studentMapper.getStudentById(1));
        // 同一个sqlSession,走缓存
        System.out.println(studentMapper.getStudentById(1));
        System.out.println(studentMapper.getStudentById(1));
        sqlSession.close();
    }

    /**
     *  <setting name="localCacheScope" value="SESSION"/>
     *  <setting name="cacheEnabled" value="true"/>
     * @throws Exception
     */
    @Test
    public void testLocalCacheClear() throws Exception {
        SqlSession sqlSession = factory.openSession(true); // true为自动提交事务
        StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);
        System.out.println(studentMapper.getStudentById(1));
        // 自动提交事务，清空了缓存
        System.out.println("增加了" + studentMapper.addStudent(buildStudent()) + "个学生");
        // 缓存为空，重新查询数据库
        System.out.println(studentMapper.getStudentById(1));
        sqlSession.close();
    }

    /**
     *   <setting name="localCacheScope" value="SESSION"/>
     *   <setting name="cacheEnabled" value="true"/>
     * @throws Exception
     */
    @Test
    public void testLocalCacheScope() throws Exception {
        SqlSession sqlSession1 = factory.openSession(true); // true为自动提交事务
        SqlSession sqlSession2 = factory.openSession(true);
        // 两个不同的sqlSession
       StudentMapper studentMapper = sqlSession1.getMapper(StudentMapper.class);
       StudentMapper studentMapper2 = sqlSession2.getMapper(StudentMapper.class);
       // 第一次从数据库中读
        System.out.println("studentMapper读取数据: " + studentMapper.getStudentById(1));
        // 一个session,从一级缓存中读
        System.out.println("studentMapper读取数据: " + studentMapper.getStudentById(1));
        // 清空sqlSession2的一级缓存
        System.out.println("studentMapper2更新了" + studentMapper2.updateStudentName("小岑",1) + "个学生的数据");
        // sqlSession1的一级缓存还在
        System.out.println("studentMapper读取数据: " + studentMapper.getStudentById(1));
        // sqlSession2一级缓存没了，所以重新从数据库读
        System.out.println("studentMapper2读取数据: " + studentMapper2.getStudentById(1));
    }


    private StudentEntity buildStudent(){
        StudentEntity studentEntity = new StudentEntity();
        studentEntity.setName("明明");
        studentEntity.setAge(20);
        return studentEntity;
    }

    /**
     *  <setting name="localCacheScope" value="SESSION"/>
     *  <setting name="cacheEnabled" value="true"/>
     * @throws Exception
     */
    @Test
    public void testCacheWithoutCommitOrClose() throws Exception {
        SqlSession sqlSession1 = factory.openSession(true); // true为自动提交事务
        SqlSession sqlSession2 = factory.openSession(true);
        // 两个不同的sqlSession
        StudentMapper studentMapper = sqlSession1.getMapper(StudentMapper.class);
        StudentMapper studentMapper2 = sqlSession2.getMapper(StudentMapper.class);
        System.out.println("studentMapper读取数据: " + studentMapper.getStudentById(1));
        // Mapper的nameSpace相同，不管是不是同一个session，都可以用二级缓存
        // 但是因为sqlSession1未提交，所以没有存入二级缓存。
        // 只能从数据库查出
        System.out.println("studentMapper2读取数据: " + studentMapper2.getStudentById(1));
    }

    /**
     *  <setting name="localCacheScope" value="SESSION"/>
     *  <setting name="cacheEnabled" value="true"/>
     * @throws Exception
     */
    @Test
    public void testCacheWithCommitOrClose() throws Exception {
        SqlSession sqlSession1 = factory.openSession(true); // true为自动提交事务
        SqlSession sqlSession2 = factory.openSession(true);
        StudentMapper studentMapper = sqlSession1.getMapper(StudentMapper.class);
        StudentMapper studentMapper2 = sqlSession2.getMapper(StudentMapper.class);
        System.out.println("studentMapper读取数据: " + studentMapper.getStudentById(1));
        // sqlSession1关闭或事务提交，则放入二级缓存
        sqlSession1.close();
        // 从二级缓存中取出
        System.out.println("studentMapper2读取数据: " + studentMapper2.getStudentById(1));
    }

    /**
     *  <setting name="localCacheScope" value="SESSION"/>
     *  <setting name="cacheEnabled" value="true"/>
     * @throws Exception
     */
    @Test
    public void testCacheWithUpdate() throws Exception {
        SqlSession sqlSession1 = factory.openSession(true);
        SqlSession sqlSession2 = factory.openSession(true);
        SqlSession sqlSession3 = factory.openSession(true);
        StudentMapper studentMapper = sqlSession1.getMapper(StudentMapper.class);
        StudentMapper studentMapper2 = sqlSession2.getMapper(StudentMapper.class);
        StudentMapper studentMapper3 = sqlSession3.getMapper(StudentMapper.class);
        System.out.println("studentMapper读取数据: " + studentMapper.getStudentById(1));
        sqlSession1.close();
        // sqlSession1关闭后，可从二级缓存中查出
        System.out.println("studentMapper2读取数据: " + studentMapper2.getStudentById(1));
        studentMapper3.updateStudentName("方方",1);
        // 提交会清空二级缓存
        sqlSession3.commit();
        // 从数据库查
        System.out.println("studentMapper2读取数据: " + studentMapper2.getStudentById(1));
    }

    /**
     *  <setting name="localCacheScope" value="SESSION"/>
     *  <setting name="cacheEnabled" value="true"/>
     * @throws Exception
     */
    @Test
    public void testCacheWithDiffererntNamespace() throws Exception {
        SqlSession sqlSession1 = factory.openSession(true); // 自动提交事务
        SqlSession sqlSession2 = factory.openSession(true); // 自动提交事务
        SqlSession sqlSession3 = factory.openSession(true); // 自动提交事务
        StudentMapper studentMapper = sqlSession1.getMapper(StudentMapper.class);
        StudentMapper studentMapper2 = sqlSession2.getMapper(StudentMapper.class);
        ClassMapper classMapper = sqlSession3.getMapper(ClassMapper.class);
        System.out.println("studentMapper读取数据: " + studentMapper.getStudentByIdWithClassInfo(1));
        sqlSession1.close();
        // 可从二级缓存中查出
        System.out.println("studentMapper2读取数据: " + studentMapper2.getStudentByIdWithClassInfo(1));
        // 提交只清空了classMapper的二级缓存，没有清空StudentMapper的二级缓存，所以读到了脏值
        classMapper.updateClassName("特色一班",1);
        sqlSession3.commit();
        // 仍然从缓存中查出
        System.out.println("studentMapper2读取数据: " + studentMapper2.getStudentByIdWithClassInfo(1));
    }

    /**
     *  <setting name="localCacheScope" value="SESSION"/>
     *  <setting name="cacheEnabled" value="true"/>
     * @throws Exception
     */
    @Test
    public void testCacheWithDiffererntNamespaceWithCacheRef() throws Exception {
        SqlSession sqlSession1 = factory.openSession(true); // 自动提交事务
        SqlSession sqlSession2 = factory.openSession(true); // 自动提交事务
        SqlSession sqlSession3 = factory.openSession(true); // 自动提交事务


        StudentMapper studentMapper = sqlSession1.getMapper(StudentMapper.class);
        StudentMapper studentMapper2 = sqlSession2.getMapper(StudentMapper.class);
        ClassMapper classMapper = sqlSession3.getMapper(ClassMapper.class);


        System.out.println("studentMapper读取数据: " + studentMapper.getStudentByIdWithClassInfo(1));
        sqlSession1.close();

        System.out.println("studentMapper2读取数据: " + studentMapper2.getStudentByIdWithClassInfo(1));
        // classMapper的二级缓存的namespace跟studentMapper是一样的，所以把studentMapper的二级缓存也清空了
        classMapper.updateClassName("特色一班",1);
        sqlSession3.commit();

        System.out.println("studentMapper2读取数据: " + studentMapper2.getStudentByIdWithClassInfo(1));
    }


}