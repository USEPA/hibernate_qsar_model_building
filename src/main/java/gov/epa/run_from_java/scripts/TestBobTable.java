package gov.epa.run_from_java.scripts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import gov.epa.databases.dev_qsar.qsar_models.entity.Bob;
import gov.epa.databases.dev_qsar.qsar_models.service.BobService;
import gov.epa.databases.dev_qsar.qsar_models.service.BobServiceImpl;
import gov.epa.run_from_java.scripts.GetExpPropInfo.DatabaseLookup;

public class TestBobTable {

	private static void tryHibernateBatchInsert(int batchSize, int totalNumber) {
		BobService bobService = new BobServiceImpl();
		Bob bob = new Bob("hibernate is the best", "cramslan");
		long t1=System.currentTimeMillis();

		List<Bob> bobs = new ArrayList<Bob>();
		for (int i = 0; i < totalNumber; i++) {
			bobs.add(bob);
		}
		bobService.batchCreate(bobs);
		long t2=System.currentTimeMillis();
		System.out.println(batchSize+"\t"+(t2-t1)+" millisec");

	}


	static void tryBatchInsert(int batchSize,int totalNumber) {
		
		String[] list = { "tmarti02", "Now is the time for all good men to hate hibernate" };
		String sql="INSERT INTO qsar_models.bobs (createdBy, description) VALUES(?,?)";		
		
//		System.out.println(sql);

		try {

			Connection conn = DatabaseLookup.getConnectionPostgres();
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
        
		int [] sizes= {10,100};
		
		tryHibernateBatchInsert(20,100);
		
        for (int size:sizes) {
        //	tryBatchInsert(size,100);
        }
		}


}
