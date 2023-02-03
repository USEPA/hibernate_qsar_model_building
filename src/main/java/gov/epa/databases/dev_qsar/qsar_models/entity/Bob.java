package gov.epa.databases.dev_qsar.qsar_models.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;

@Entity
@Table(name="bobs", schema="qsar_models")
public class Bob {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(name="description")
	private String description;

	@Column(name="createdBy")
	private String createdBy;

	
	public Bob() {}

	public Bob(String description) {			
		this.description=description;
	}


	static void tryBatchInsert(int batchSize,int totalNumber) {
		
		String[] list = { "tmarti02", "Now is the time for all good men to hate hibernate" };
		String sql="INSERT INTO qsar_models.bobs (createdBy, description) VALUES(?,?)";		
		
//		System.out.println(sql);

		try {

			Connection conn = DatabaseLookup.getConnection();
			conn.setAutoCommit(false);

			PreparedStatement prep = conn.prepareStatement(sql);

			long t1=System.currentTimeMillis();

			
			for (int counter = 1; counter <= totalNumber; counter++) {

				for (int j = 0; j < list.length; j++) {
					prep.setString(j + 1, list[j]);
//		               System.out.println((i+1)+"\t"+list.get(i));
				}

				prep.addBatch();

				if (counter % batchSize == 0) {
					// System.out.println(counter);
					prep.executeBatch();
				}

			}

			int[] count = prep.executeBatch();// do what's left
			

			long t2=System.currentTimeMillis();
			System.out.println(batchSize+"\t"+(t2-t1)+" millisec");
			
			conn.commit();
			
//			conn.setAutoCommit(true);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	public static void main(String[] args) {
        
		int [] sizes= {100,1000,10000};
		
        for (int size:sizes)
        	tryBatchInsert(size,10000);
		
	}


}


