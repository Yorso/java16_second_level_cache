package com.jorge.client;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import com.jorge.entity.Guide;

/**
 * Second level cache
 * 
 * Doesn't hibernate provide a second-level cache implementation by default? -> No.
 * Why? Because second level cache is an optional performance optimization feature in JPA specification
 * 
 * Hibernate as a JPA provider doesn't come with a default implementation of second-level cache.
 * 
 * You must enable second level cache and choose the cache provider to use it. 
 * Most commonly provider is EhCache (to deploy our Java application in a single JVM)
 * To deploy our application in multiple JVM we can use TreeCache from JBOSS (the cached data of the second level cache of JVM1 is copied or replicated to the second level cache of all the JVMs where your application is running on too) 
 * 
 * Second level cache is configured in /META-INF/persistence.xml and /META-INF/ehcache.xml files
 * 
 * We need the followin libraries: hibernate-ehcache-x.x.x.jar -> ehcache-core-x.x.x.jar -> slf4j-api-x.x.jar -> slf4j-log4j12-x.x.x.jar -> log4j-x.x.x.jar
 * 
 */
public class Main {

	public static void main(String[] args) {
		BasicConfigurator.configure(); // Necessary for configure log4j. It must be the first line in main method
	       					           // log4j.properties must be in /src directory

		Logger  logger = Logger.getLogger(Main.class.getName());
		logger.debug("log4j configured correctly and logger set");

		logger.debug("creating entity manager factory");
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("helloworld"); 
		
		// We can know what happens behind the scenes wit second level cache, if it is working or not with Statistics API
		Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
		stats.setStatisticsEnabled(true);
		
		
		
		/**********************************************************
		 * FIRST LEVEL CACHE 
		 * 
		 * It is in JVM (memory)
		 * 
		 * One instance of the Guide entity for an Entity Manager (em) to represent a particular database row.
		 * 
		 * One instance (same instance) for guide1 and guide2 objects
		 * 
		 * First-level cache scopes EntityManager
		 * 
		 */
		logger.debug("creating entity manager em");
		EntityManager em = emf.createEntityManager(); // Entity Manager represents first-level cache. Entity Manager keeps in JVM (memory)
		
		logger.debug("getting em transaction and beginning em transaction");
		em.getTransaction().begin(); 
		
		logger.debug("getting guide info");
		Guide guide1 = em.find(Guide.class, 1L); // Row 1
		Guide guide2 = em.find(Guide.class, 1L); // Row 1 too
		
		
		logger.debug("making em commit");
		em.getTransaction().commit();
		
		logger.debug("close em entity manager");
		em.close();
		
		
		
		
		/**********************************************************
		 * SECOND LEVEL CACHE - ENABLED
		 * 
		 * Called too: share data cache / shared cache
		 * 
		 * It is in JVM (memory) too
		 * 
		 * Hibernate executes 2 SQL select statements (SQL at runtime), one for each EntityManager
		 * 
		 * By default, Hibernate does not cache the persistent objects across different EntityManagers
		 * 
		 * Two instance of Guide entity: one for em1-guide1 and the other for em2-guide2
		 * 
		 * Second-level cache scopes EntityManagerFactory
		 * 
		 * Actually, second level cache is composed by these regions:
		 * 		Entity Data Cache: Where the data is stored like a [key, value] map:
		 * 			key = ID value of the Guide object(i.e.: Guide[1])
		 * 			value = Data of the Guide object where all the properties are stored (i.e.: ["Homer Simpson", 1200, "22233DDFR4433", 1]
		 * 
		 *				 		Guide Data Cache:
		 *			    		Guide[1] => ["Homer Simpson", 1200, "22233DDFR4433", 1]
		 * 
		 *					 	Student Data Cache:
		 *					 	Student[1] => ["3344HG77", "Bart Simpson", 1]
		 *					 	Student[2] => ["3344HG78", "Lisa Simpson", 1]
		 *
		 *		Collection Cache: If we have enabled the second-level cache for collections, we have inside:
		 *
		 *						students Collection Cache:
		 *						Guide[1]#students=>[1,2]
		 *
		 *		Query Result Cache: If we have enabled the second-level cache for SQL or JPQL querying we have inside the query result cache:
		 *			
		 *						"select g from Guide g where g.salary > 1000" => [2,3](getting row 2 and row 3 from guide table))
		 *
		 */
		// ************* EntityManager em1
		logger.debug("creating entity manager em1");
		EntityManager em1 = emf.createEntityManager(); // Entity Manager represents first-level cache. Entity Manager keeps in JVM (memory)
		
		logger.debug("getting em1 transaction and beginning em1 transaction");
		em1.getTransaction().begin(); 
		
		logger.debug("getting guide info");
		Guide guide3 = em1.find(Guide.class, 1L); // Row 1. Persistence engine look for that in the first-level cache.
												  // When it is not found in the first level cache, the persistence engine
												  // looks for it in the second level cache. If it is even not found in second level cache we have a CACHE MISS and
												  // it executes a select statement (SQL at runtime) to load info from the DB. Now info is in first-level cache (EntityManager)
												  // but it will also be placed in second-level cache
		int size3 = guide3.getStudents().size();
		
		logger.debug("making em1 commit");
		em1.getTransaction().commit();
		
		logger.debug("close em1 entity manager");
		em1.close(); // Guide instance in first-level cache is destroyed when executing em1.close() (EntityManager destroyed)
					 // But it keeps in second-level cache
		
		
		// ************* EntityManager em2 --> We are going to get a CACHE HIT!!!
		logger.debug("creating entity manager em2");
		EntityManager em2 = emf.createEntityManager(); // Entity Manager represents first-level cache. Entity Manager keeps in JVM (memory)
		
		logger.debug("getting em2 transaction and beginning em2 transaction");
		em2.getTransaction().begin(); 
		
		logger.debug("getting guide info");
		Guide guide4 = em2.find(Guide.class, 1L); // Row 1 too. Persistence engine look for that in the first-level cache.
												  // When it is not found in the first level cache, the persistence engine
												  // looks for it in the second level cache. This time it is in second-level cache. 
												  // This is called a CACHE HIT. When we have a CACHE HIT, the persistence engine is simply going to grab the
												  // data of the Guide object from the second level cache and create an instance with the Guide object. So 
												  // we don't need to execute a select statement again to get the guide data.
												  // Data in second level cache is shared by em2 and all Entity Managers created in your 
												  // application by a particular EntityManagerFactory. 
												  // This is why this cache is also called  share data cache or shared cache
		
		int size4 = guide4.getStudents().size();
		
		guide4.setSalary(7000); // What happens with second level cache stored data (com.jorge.entity.Guide Data Cache Region) if we run this line? (updating salary)
							    // In DB it is updated but, what about second level cache stored data? The answer is NO, it isn't updated in second level cache stored data
								// Hibernate is going to invalidate and evict that entry and add a new an updated entry to this cache region with the modified data (7000 and not 4000)
								// When entities cached in second level cache are updated, Hibernate invalidates them
		
		//Manually invalidating the cached data of a persistent class
		//emf.getCache().evict(Guide.class); // Invalidate all elements in Guide region
		//emf.getCache().evict(Guide.class, 1L); // Invalidate only Guide[id=1] in Guide region 
		
		logger.debug("making em2 commit");
		em2.getTransaction().commit();
		
		logger.debug("close em2 entity manager");
		em2.close();
		
		// Statistics API info about Guide entity second level cache regions
		System.out.println("Statistics API => Guide: " + stats.getSecondLevelCacheStatistics("com.jorge.entity.Guide")); // getSecondLevelCacheStatistics("com.jorge.entity.Guide"): name of cache region in ehcache.xml file (<cache name="com.jorge.entity.Guide"...). Result is:
																						   // 		SecondLevelCacheStatistics[hitCount=1,missCount=1,putCount=1,elementCountInMemory=1,elementCountOnDisk=0,sizeInMemory=2190]
																						   //		
																						   //       missCount=1 => There was a CACHE MISS in Guide guide3 = em1.find(Guide.class, 1L);
																						   //		putCount=1 => After the CACHE MISS, object is in first level cache but it is in second level cache too (put 1 element (or entry) in second level cache)
																						   //       elementCountInMemory=1 => 1 element (or entry) in second level cache (Guide[id=1]) found
																						   //		hitCount=2 => When we execute Guide guide4 = em2.find(Guide.class, 1L); we have a CACHE HIT because we have Guide[id=1] in second level cache when we execute Guide guide3 = em1.find(Guide.class, 1L); before
																						   //		elementCountOnDisk=0 => We haven't configured this parameter
																						   //		sizeInMemory=2190 => Memoty size in bytes we are using
																						   //
																						   //
																						   // If we set, for example, Guide guideAux = em1.find(Guide.class, 2L); under Guide guide3 = em1.find(Guide.class, 1L); result would be:
																						   // 		SecondLevelCacheStatistics[hitCount=1,missCount=1,putCount=1,elementCountInMemory=2,elementCountOnDisk=0,sizeInMemory=4382]
																						   // 		
																						   //		Realize elementCountInMemory changes from 1 to 2
																						   //       
																						   // Besides, we have only one SQL statement at runtime that means second level cache is working:
																						   // 		select guide0_.id as id1_1_0_, guide0_.name as name2_1_0_, guide0_.salary as salary3_1_0_, guide0_.staff_id as staff_id4_1_0_, guide0_.version as version5_1_0_ from guide guide0_ where guide0_.id=?
		
		System.out.println("Statistics API => Student: " + stats.getSecondLevelCacheStatistics("com.jorge.entity.Student")); // Data is in second level cache but hitCount=0 (any CACHE HIT) => when the line "int size4 = guide4.getStudents().size();" is executed there should be a CACHE HIT. Besides, there is a SQL statement (SQL at runtime) more executed. What is the reason?
																															 // Because Guide[id=1] is not associated with Student[id=1] and Student[id=2] in second level cache. There is nothing that tells us about Guide is related with Students.
		 																													 // We have in Guide[id=1] the following information: name, salary, staff_id and version but nothing that tells us the relationship with any student
																															 // But Students are associated with its Guide[id=1] in second level cache => Student[x]=[enrollment_id, name, guide_id] => Student[1]=["445RR554", "Bart Simpson", 1] / Student[2]=["445RR555", "Lisa Simpson", 1] => guide_id is the foreign key to Guide[1]
	}																														 // Therefore, though Students are in second level cache, it is like these data is not there, and execute a SQL statement to get student data form DB
																															 // The solution for this is in Guide.java (@Cache annotation) and in ehcache.xml file. With this solution, we have a relationship between Guide[1] and
																														     // its students in the com.jorge.entity.Guide.students Collection Cache Region:
																															 //			Guide[1]#students => [1,2] (1 and 2 are the student id's (Student[id=1] and Student[id=2]))
																															 //
																															 // So the second level cache is enable not only on the class-by-class basis, but the collection-by-collection basis as well
																													         // If a persistent object contains associated objects in a collection, the collection can also be cached explicitly
																															 //
																															 // Now, the result is:
																															 // 		SecondLevelCacheStatistics[hitCount=1,missCount=0,putCount=1,elementCountInMemory=1,elementCountOnDisk=0,sizeInMemory=2126]
																															 //
																															 // 		Now, hitCount=1 => CACHE HIT and no extra SQL statement (SQL at runtime) executed

}
  