package greencity.mapping.certificate;

import greencity.ModelUtils;
import greencity.dto.certificate.CertificateDtoForSearching;
import greencity.entity.order.Certificate;
import greencity.mapping.certificate.CertificateDtoForSearchingMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CertificateDtoForSearchingMapperTest {
    @InjectMocks
    CertificateDtoForSearchingMapper certificateDtoForSearchingMapper;

    @Test
    void convertTestWithOrderNull() {
        Certificate expected = ModelUtils.getCertificate();
        CertificateDtoForSearching certificateDtoForSearching = certificateDtoForSearchingMapper.convert(expected);

        assertEquals(expected.getCertificateStatus(), certificateDtoForSearching.getCertificateStatus());
        assertEquals(expected.getCode(), certificateDtoForSearching.getCode());
        assertEquals(expected.getCreationDate(), certificateDtoForSearching.getCreationDate());
        assertEquals(expected.getExpirationDate(), certificateDtoForSearching.getExpirationDate());
        assertEquals(expected.getPoints(), certificateDtoForSearching.getPoints());
        assertEquals(expected.getDateOfUse(), certificateDtoForSearching.getDateOfUse());
        assertEquals(null, certificateDtoForSearching.getOrderId());
    }

    @Test
    void convertTestWithOrderNotNull() {
        Certificate expected = ModelUtils.getCertificate();
        expected.setOrder(ModelUtils.getOrder());
        CertificateDtoForSearching certificateDtoForSearching = certificateDtoForSearchingMapper.convert(expected);

        assertEquals(expected.getOrder().getId(), certificateDtoForSearching.getOrderId());
    }
}
