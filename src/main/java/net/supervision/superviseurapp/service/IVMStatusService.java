package net.supervision.superviseurapp.service;

import java.io.IOException;
import java.util.List;

public interface IVMStatusService {


    void testVMsFromFile(String filePath) throws IOException;
    List<String[]> VMReadIpPortFromFile(String filePath) throws IOException;

}
