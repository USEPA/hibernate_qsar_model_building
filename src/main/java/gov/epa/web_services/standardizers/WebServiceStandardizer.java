package gov.epa.web_services.standardizers;

import java.io.File;
import java.util.List;

import gov.epa.web_services.WebService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

/**
 * Descriptor generation class that handles web service access
 * @author GSINCL01
 *
 */
public class WebServiceStandardizer extends Standardizer {

	private WebService ws;

	public WebServiceStandardizer(String server, int port, String standardizerName, String standardizerType, boolean useBatchStandardize) {
		this.ws = new WebService(server, port);
		this.standardizerName = standardizerName;
		this.standardizerType = standardizerType;
		this.useBatchStandardize = useBatchStandardize;
	}

	@Override
	public StandardizeResponseWithStatus callStandardize(String smiles) {
		HttpResponse<StandardizeResponse> response = Unirest.get(ws.address+"/standardize")
				.queryString("smiles", smiles)
				.asObject(StandardizeResponse.class);
		
		return StandardizeResponseWithStatus.fromHttpStandardizeResponse(response);
	}
	
	@Override
	public BatchStandardizeResponseWithStatus callBatchStandardize(List<String> smiles) {
		String smilesString = String.join(",", smiles);
		HttpResponse<BatchStandardizeResponse> response = Unirest.get(ws.address+"/batch/standardize/")
				.queryString("smiles", smilesString)
				.asObject(BatchStandardizeResponse.class);
		
		return BatchStandardizeResponseWithStatus.fromHttpBatchStandardizeResponse(response);
	}
	
	@Override
	public BatchStandardizeResponseWithStatus callBatchStandardize(String filePath) {
		HttpResponse<BatchStandardizeResponse> response = Unirest.post(ws.address+"/batch/standardize/")
				.field("file", new File(filePath))
				.header("Accept", "application/json")
				.asObject(BatchStandardizeResponse.class);
		
		return BatchStandardizeResponseWithStatus.fromHttpBatchStandardizeResponse(response);
	}

	@Override
	public HttpResponse<String> callQsarReadyStandardizePost(String smiles, boolean full) {
		// TODO Auto-generated method stub
		return null;
	}
}
