package com.jorge.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

// ONE SIDE entity => INVERSE END

@Entity
@Cacheable // or @Cacheable(true) => Cacheable for second level cache.
/**
* Cache Concurrency Strategy:
* 
* What happens if tow EntityManagers (in first level cache) with Guide[id=1] object each one want to update Guide[id=1] object in second level cache (all with same id=1)?
* We can use Cache Concurrency Strategy
* 
* By default, @Cacheable is CacheConcurrencyStrategy.READ_WRITE which is equivalent to READ_COMMITTED transaction isolation level
* 
* A cache concurrency strategy defines a transaction isolation level for an entry in a cache region
*
* From top to bottom: Higher Performance, Higher Scalability, Lesser Isolation
* 
* 		TRANSACTIONAL -> It's cluster scope. Application is working on a cluster of JVM. 
* 						 Use TreeCache as cache provider.When your application is deployed in 
*                        multiple JVM (cluster). read-mostly data; equivalent to REPEATABLE_READ
* 
*  		READ_WRITE -> It's process scope. Application is deployed in a single JVM. 
*                     Use EhCache as cache provider. Read-mostly data; equivalent to READ_COMMITTED
* 		
* 		NONSTRICT_READ_WRITE -> It's process scope. Application is deployed in a single JVM. 
*                               Use EhCache as cache provider. Data hardly ever changes. Users are unlikely 
*                               to update the same data simultaneously.
* 
* 		READ_ONLY -> It's process scope. Application is deployed in a single JVM. Use EhCache as cache provider. Good when data is never modified; e.g. country codes	
* 
* 
* To change the Cache Concurrency Strategy:
* 		@Cacheable
* 		@org.hibernate.annotations.Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
* 
*/
@Table(name="guide")
public class Guide {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	private Long id;
	
	@Column(name="staff_id", nullable=false)
	private String staffId;
	
	@Column
	private String name;
	
	@Column(name="salary")
	private Integer salary;
	
	@Version
	private Integer version;
	
	
	/**
	 * Caching associations in second level cache
	 * 
	 * By default, association objects are not cached
	 * 
	 * The reason to cache associations is to avoid extra calls to the database
	 * 
	 */
	// Lazy loading by default in collection points
	@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE) // This is the solution to associate Guide with Students (several students) in second level cache entity data region. Check ehcache.xml file too 
	@OneToMany(mappedBy="guide", cascade={CascadeType.PERSIST})
	private Set<Student> students = new HashSet<Student>();
	
	public Guide() {}
	
	public Guide(String staffId, String name, Integer salary) {
		this.staffId = staffId;
		this.name = name;
		this.salary = salary;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStaffId() {
		return staffId;
	}

	public void setStaffId(String staffId) {
		this.staffId = staffId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getSalary() {
		return salary;
	}

	public void setSalary(Integer salary) {
		this.salary = salary;
	}

	public Set<Student> getStudents() {
		return students;
	}

	public void setStudents(Set<Student> students) {
		this.students = students;
	}

	@Override
	public String toString() {
		return "Guide [id=" + id + ", staffId=" + staffId + ", name=" + name + ", salary=" + salary +  "]";
	}
	
}
