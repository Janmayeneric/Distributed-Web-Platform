package relation;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;

import org.springframework.data.annotation.Id;

/**
 * it is a storage show the relation between community and member
 */
@Entity
public class CommunityMember {

		private @Id @GeneratedValue String id;
		private String userId;
		private String communityName;
		
		public CommunityMember() {
			
		}
		
		public CommunityMember(String userId, String communityName) {
			this.userId = userId;
			this.communityName = communityName;
		}
		
		public String getUserId() {
			return this.userId;
		}
		
		public String getCommunityName() {
			return this.communityName;
		}
		
		public void setUserId(String userId) {
			this.userId = userId;
		}
		
		public void setCommunityName(String communityName) {
			this.communityName = communityName;
		}
}

