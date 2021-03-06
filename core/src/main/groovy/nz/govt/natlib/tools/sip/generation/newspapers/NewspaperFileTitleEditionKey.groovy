package nz.govt.natlib.tools.sip.generation.newspapers

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.Sortable
import groovy.transform.ToString
import groovy.util.logging.Log4j2

@Canonical
@EqualsAndHashCode
@Sortable(includes = ['titleCode', 'sectionCode' ])
@ToString
@Log4j2
class NewspaperFileTitleEditionKey {
    String titleCode
    String sectionCode
}
