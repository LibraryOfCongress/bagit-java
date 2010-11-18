package gov.loc.repository.bagit;

public interface BagWriter {
	void open(Bag bag);
	void close();
	void writeTagFile(String filepath, BagFile bagFile);
	void writePayloadFile(String filepath, BagFile bagFile);
}
