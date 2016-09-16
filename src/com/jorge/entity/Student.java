package com.jorge.entity;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

// MANY SIDE entity => OWNER of this bidirectional relationship
// The owner of the relationship is responsible for the association column(s) update

@Entity
@Cacheable // Need to caching associations. Check ehcache.xml file
public class Student {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	private Long id;
	
	@Column(name="enrollment_id", nullable=false)
	private String enrollmentId;
	
	private String name;
	
	@ManyToOne(fetch=FetchType.LAZY) // eager loading by default in single points
	@JoinColumn(name="guide_id")
	private Guide guide;
	
	public Student() {}
	
	public Student(String enrollmentId, String name, Guide guide) {
		this.enrollmentId = enrollmentId;
		this.name = name;
		this.guide = guide;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEnrollmentId() {
		return enrollmentId;
	}

	public void setEnrollmentId(String enrollmentId) {
		this.enrollmentId = enrollmentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Guide getGuide() {
		return guide;
	}

	public void setGuide(Guide guide) {
		this.guide = guide;
	}

	@Override
	public String toString() {
		return "Student [id=" + id + ", enrollmentId=" + enrollmentId + ", name=" + name + ", guide=" + guide + "]";
	}
	
	@Override
	public int hashCode(){
		return new HashCodeBuilder().append(enrollmentId).toHashCode();
	}
	
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof Student)) return false;
		Student other = (Student) obj;
		return new EqualsBuilder().append(enrollmentId, other.enrollmentId).isEquals();
	}

}
