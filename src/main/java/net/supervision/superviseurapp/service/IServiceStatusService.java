package net.supervision.superviseurapp.service;

import java.io.IOException;
import java.util.List;

public interface IServiceStatusService {

    void testServicesFromFile(String filePath) throws IOException;
    List<String[]> MSReadIpPortFromFile(String filePath) throws IOException;


}
