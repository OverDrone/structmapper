package org.stuctmapper.instructions;

import org.stuctmapper.log.Logger;

public class LoggableService {
    protected Logger logger;
    
    protected LoggableService(final Logger logger) {
        this.logger = logger;
    }
}
