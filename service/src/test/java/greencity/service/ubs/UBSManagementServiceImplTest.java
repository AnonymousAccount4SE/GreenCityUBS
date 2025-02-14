package greencity.service.ubs;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.constant.AppConstant;
import greencity.constant.OrderHistory;
import greencity.dto.bag.AdditionalBagInfoDto;
import greencity.dto.bag.BagInfoDto;
import greencity.dto.bag.BagMappingDto;
import greencity.dto.certificate.CertificateDtoForSearching;
import greencity.dto.employee.EmployeePositionDtoRequest;
import greencity.dto.order.AdminCommentDto;
import greencity.dto.order.CounterOrderDetailsDto;
import greencity.dto.order.DetailsOrderInfoDto;
import greencity.dto.order.EcoNumberDto;
import greencity.dto.order.ExportDetailsDto;
import greencity.dto.order.ExportDetailsDtoUpdate;
import greencity.dto.order.NotTakenOrderReasonDto;
import greencity.dto.order.OrderAddressDtoResponse;
import greencity.dto.order.OrderAddressExportDetailsDtoUpdate;
import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderDetailInfoDto;
import greencity.dto.order.OrderDetailStatusDto;
import greencity.dto.order.OrderDetailStatusRequestDto;
import greencity.dto.order.OrderInfoDto;
import greencity.dto.order.ReadAddressByOrderDto;
import greencity.dto.order.UpdateAllOrderPageDto;
import greencity.dto.order.UpdateOrderPageAdminDto;
import greencity.dto.pageble.PageableDto;
import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.dto.payment.PaymentInfoDto;
import greencity.dto.user.AddBonusesToUserDto;
import greencity.dto.user.AddingPointsToUserDto;
import greencity.dto.violation.ViolationsInfoDto;
import greencity.entity.order.Bag;
import greencity.entity.order.Certificate;
import greencity.entity.order.Order;
import greencity.entity.order.OrderPaymentStatusTranslation;
import greencity.entity.order.OrderStatusTranslation;
import greencity.entity.order.Payment;
import greencity.entity.order.Refund;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.EmployeeOrderPosition;
import greencity.entity.user.employee.Position;
import greencity.entity.user.employee.ReceivingStation;
import greencity.entity.user.ubs.OrderAddress;
import greencity.enums.OrderPaymentStatus;
import greencity.enums.OrderStatus;
import greencity.enums.SortingOrder;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.repository.BagRepository;
import greencity.repository.CertificateRepository;
import greencity.repository.EmployeeOrderPositionRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.OrderAddressRepository;
import greencity.repository.OrderDetailRepository;
import greencity.repository.OrderPaymentStatusTranslationRepository;
import greencity.repository.OrderRepository;
import greencity.repository.OrderStatusTranslationRepository;
import greencity.repository.PaymentRepository;
import greencity.repository.PositionRepository;
import greencity.repository.ReceivingStationRepository;
import greencity.repository.RefundRepository;
import greencity.repository.ServiceRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.UserRepository;
import greencity.service.locations.LocationApiService;
import greencity.service.notification.NotificationServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static greencity.ModelUtils.ORDER_DETAIL_STATUS_DTO;
import static greencity.ModelUtils.TEST_ADDITIONAL_BAG_INFO_DTO;
import static greencity.ModelUtils.TEST_ADDITIONAL_BAG_INFO_DTO_LIST;
import static greencity.ModelUtils.TEST_BAG;
import static greencity.ModelUtils.TEST_BAG_INFO_DTO;
import static greencity.ModelUtils.TEST_BAG_LIST;
import static greencity.ModelUtils.TEST_BAG_MAPPING_DTO_LIST;
import static greencity.ModelUtils.TEST_MAP_ADDITIONAL_BAG_LIST;
import static greencity.ModelUtils.TEST_ORDER;
import static greencity.ModelUtils.TEST_ORDER_ADDRESS_DTO_RESPONSE;
import static greencity.ModelUtils.TEST_ORDER_ADDRESS_DTO_UPDATE;
import static greencity.ModelUtils.TEST_ORDER_DETAILS_INFO_DTO_LIST;
import static greencity.ModelUtils.TEST_PAYMENT_LIST;
import static greencity.ModelUtils.TEST_USER;
import static greencity.ModelUtils.UPDATE_ORDER_PAGE_ADMIN_DTO;
import static greencity.ModelUtils.getAddBonusesToUserDto;
import static greencity.ModelUtils.getAdminCommentDto;
import static greencity.ModelUtils.getBag1list;
import static greencity.ModelUtils.getBag2list;
import static greencity.ModelUtils.getBag3list;
import static greencity.ModelUtils.getBagInfoDto;
import static greencity.ModelUtils.getBaglist;
import static greencity.ModelUtils.getCertificateList;
import static greencity.ModelUtils.getEcoNumberDto;
import static greencity.ModelUtils.getEmployee;
import static greencity.ModelUtils.getExportDetailsRequest;
import static greencity.ModelUtils.getExportDetailsRequestToday;
import static greencity.ModelUtils.getFormedOrder;
import static greencity.ModelUtils.getInfoPayment;
import static greencity.ModelUtils.getManualPayment;
import static greencity.ModelUtils.getManualPaymentRequestDto;
import static greencity.ModelUtils.getOrder;
import static greencity.ModelUtils.getOrderAddress;
import static greencity.ModelUtils.getOrderDoneByUser;
import static greencity.ModelUtils.getOrderExportDetails;
import static greencity.ModelUtils.getOrderExportDetailsWithDeliverFromTo;
import static greencity.ModelUtils.getOrderExportDetailsWithExportDate;
import static greencity.ModelUtils.getOrderExportDetailsWithExportDateDeliverFrom;
import static greencity.ModelUtils.getOrderExportDetailsWithExportDateDeliverFromTo;
import static greencity.ModelUtils.getOrderExportDetailsWithNullValues;
import static greencity.ModelUtils.getOrderForGetOrderStatusData2Test;
import static greencity.ModelUtils.getOrderForGetOrderStatusEmptyPriceDetails;
import static greencity.ModelUtils.getOrderStatusPaymentTranslations;
import static greencity.ModelUtils.getOrderStatusTranslation;
import static greencity.ModelUtils.getOrderStatusTranslations;
import static greencity.ModelUtils.getOrderUserFirst;
import static greencity.ModelUtils.getOrderWithoutPayment;
import static greencity.ModelUtils.getOrdersStatusFormedDto;
import static greencity.ModelUtils.getPayment;
import static greencity.ModelUtils.getReceivingList;
import static greencity.ModelUtils.getReceivingStation;
import static greencity.ModelUtils.getRefund;
import static greencity.ModelUtils.getService;
import static greencity.ModelUtils.getStatusTranslation;
import static greencity.ModelUtils.getTariffsInfo;
import static greencity.ModelUtils.getTestDetailsOrderInfoDto;
import static greencity.ModelUtils.getTestOrderDetailStatusRequestDto;
import static greencity.ModelUtils.getTestUser;
import static greencity.ModelUtils.updateAllOrderPageDto;
import static greencity.ModelUtils.updateOrderPageAdminDto;
import static greencity.constant.ErrorMessage.EMPLOYEE_NOT_FOUND;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.ORDER_CAN_NOT_BE_UPDATED;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UBSManagementServiceImplTest {
    @Mock(lenient = true)
    OrderAddressRepository orderAddressRepository;
    @Mock
    private FileService fileService;

    @Mock(lenient = true)
    OrderRepository orderRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CertificateRepository certificateRepository;

    @Mock(lenient = true)
    private ModelMapper modelMapper;

    @Mock
    private ReceivingStationRepository receivingStationRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private EmployeeOrderPositionRepository employeeOrderPositionRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private BagRepository bagRepository;

    @Mock
    private UserRemoteClient userRemoteClient;

    @Mock(lenient = true)
    private NotificationServiceImpl notificationService;
    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @InjectMocks
    private UBSManagementServiceImpl ubsManagementService;

    @Mock
    private EventService eventService;

    @Mock
    private OrderStatusTranslationRepository orderStatusTranslationRepository;

    @Mock
    private OrderPaymentStatusTranslationRepository orderPaymentStatusTranslationRepository;

    @Mock
    private UBSClientServiceImpl ubsClientService;

    @Mock
    private UBSManagementServiceImpl ubsManagementServiceMock;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    OrdersAdminsPageService ordersAdminsPageService;

    @Mock
    TariffsInfoRepository tariffsInfoRepository;

    @Mock
    private LocationApiService locationApiService;

    @Mock
    RefundRepository refundRepository;

    @Test
    void getAllCertificates() {
        Pageable pageable =
            PageRequest.of(0, 5, Sort.by(Sort.Direction.fromString(SortingOrder.DESC.toString()), "points"));
        CertificateDtoForSearching certificateDtoForSearching = ModelUtils.getCertificateDtoForSearching();
        List<Certificate> certificates =
            Collections.singletonList(ModelUtils.getCertificate());
        List<CertificateDtoForSearching> certificateDtoForSearchings =
            Collections.singletonList(certificateDtoForSearching);
        PageableDto<CertificateDtoForSearching> certificateDtoForSearchingPageableDto =
            new PageableDto<>(certificateDtoForSearchings, certificateDtoForSearchings.size(), 0, 1);
        Page<Certificate> certificates1 = new PageImpl<>(certificates, pageable, certificates.size());
        when(modelMapper.map(certificates.get(0), CertificateDtoForSearching.class))
            .thenReturn(certificateDtoForSearching);
        when(certificateRepository.findAll(pageable)).thenReturn(certificates1);
        PageableDto<CertificateDtoForSearching> actual =
            ubsManagementService.getAllCertificates(pageable, "points", SortingOrder.DESC);
        assertEquals(certificateDtoForSearchingPageableDto, actual);
    }

    @Test
    void checkOrderNotFound() {
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.getAddressByOrderId(10000000L));
    }

    @Test
    void getAddressByOrderId() {
        Order order = getOrder();
        ReadAddressByOrderDto readAddressByOrderDto = ModelUtils.getReadAddressByOrderDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(ubsManagementService.getAddressByOrderId(1L)).thenReturn(readAddressByOrderDto);
        Assertions.assertNotNull(order);
    }

    @Test
    void checkPaymentNotFound() {
        Assertions.assertThrows(NotFoundException.class, () -> ubsManagementService.getOrderDetailStatus(100L));
    }

    @Test
    void returnExportDetailsByOrderId() {
        ExportDetailsDto expected = ModelUtils.getExportDetails();
        Order order = getOrderExportDetails();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        List<ReceivingStation> stations =
            Arrays.asList(ReceivingStation.builder().name("a").build(), ReceivingStation.builder().name("b").build());
        when(receivingStationRepository.findAll()).thenReturn(stations);

        assertEquals(expected, ubsManagementService.getOrderExportDetails(1L));
    }

    @Test
    void updateExportDetailsByOrderId() {
        ExportDetailsDtoUpdate dto = getExportDetailsRequest();
        Order order = getOrderExportDetails();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        var receivingStation = getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));

        List<ReceivingStation> stations = List.of(new ReceivingStation());
        when(receivingStationRepository.findAll()).thenReturn(stations);

        ubsManagementService.updateOrderExportDetails(order.getId(), dto, "abc");

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void updateExportDetailsNotSuccessfulByOrderId() {
        ExportDetailsDtoUpdate dto = getExportDetailsRequest();
        Order order = getOrderExportDetailsWithNullValues();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        var receivingStation = getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        List<ReceivingStation> stations = List.of(new ReceivingStation());
        when(receivingStationRepository.findAll()).thenReturn(stations);

        ubsManagementService.updateOrderExportDetails(order.getId(), dto, "abc");

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void checkStationNotFound() {
        Assertions.assertThrows(NotFoundException.class, () -> ubsManagementService.getOrderExportDetails(100L));
    }

    @ParameterizedTest
    @MethodSource("provideManualPaymentRequestDto")
    void saveNewManualPayment(ManualPaymentRequestDto paymentDetails, MultipartFile image) {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = getFormedOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Payment payment = getManualPayment();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(OrderHistory.ADD_PAYMENT_MANUALLY + 1, "Петро" + "  " + "Петренко", order);
        ubsManagementService.saveNewManualPayment(1L, paymentDetails, image, "test@gmail.com");

        verify(eventService, times(1))
            .save("Додано оплату №1", "Петро  Петренко", order);
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    private static Stream<Arguments> provideManualPaymentRequestDto() {
        return Stream.of(Arguments.of(ManualPaymentRequestDto.builder()
            .settlementdate("02-08-2021").amount(500L).receiptLink("link").paymentId("1").build(), null),
            Arguments.of(ManualPaymentRequestDto.builder()
                .settlementdate("02-08-2021").amount(500L).imagePath("path").paymentId("1").build(),
                Mockito.mock(MultipartFile.class)));
    }

    @Test
    void checkDeleteManualPayment() {
        Employee employee = getEmployee();
        Order order = getFormedOrder();
        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(employee));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(getManualPayment()));
        doNothing().when(paymentRepository).deletePaymentById(1L);
        doNothing().when(fileService).delete("");
        doNothing().when(eventService).save(OrderHistory.DELETE_PAYMENT_MANUALLY + getManualPayment().getPaymentId(),
            employee.getFirstName() + "  " + employee.getLastName(),
            getOrder());
        ubsManagementService.deleteManualPayment(1L, "abc");
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).deletePaymentById(1L);
    }

    @Test
    void checkUpdateManualPayment() {
        Employee employee = getEmployee();
        Order order = getFormedOrder();
        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(employee));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(getManualPayment()));
        when(paymentRepository.save(any())).thenReturn(getManualPayment());
        when(bagRepository.findBagsByOrderId(order.getId())).thenReturn(getBaglist());
        doNothing().when(eventService).save(OrderHistory.UPDATE_PAYMENT_MANUALLY + 1,
            employee.getFirstName() + "  " + employee.getLastName(),
            getOrder());
        ubsManagementService.updateManualPayment(1L, getManualPaymentRequestDto(), null, "abc");
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(any());
        verify(eventService, times(2)).save(any(), any(), any());
        verify(fileService, times(0)).delete(null);
        verify(bagRepository).findBagsByOrderId(order.getId());
    }

    @Test
    void saveNewManualPaymentWithZeroAmount() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = getFormedOrder();
        order.setPointsToUse(0);
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        Payment payment = getManualPayment();
        payment.setAmount(0L);
        order.setPayment(singletonList(payment));
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementdate("02-08-2021").amount(0L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(any(), any(), any());
        ubsManagementService.saveNewManualPayment(1L, paymentDetails, null, "test@gmail.com");
        verify(employeeRepository, times(2)).findByEmail(anyString());
        verify(eventService, times(1)).save(OrderHistory.ADD_PAYMENT_MANUALLY + 1,
            "Петро  Петренко", order);
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void saveNewManualPaymentWithHalfPaidAmount() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = getFormedOrder();
        order.setPointsToUse(0);
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        order.setOrderPaymentStatus(OrderPaymentStatus.HALF_PAID);
        Payment payment = getManualPayment();
        payment.setAmount(50_00L);
        order.setPayment(singletonList(payment));
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementdate("02-08-2021").amount(50_00L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBag1list());
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(any(), any(), any());
        ubsManagementService.saveNewManualPayment(1L, paymentDetails, null, "test@gmail.com");
        verify(employeeRepository, times(2)).findByEmail(anyString());
        verify(eventService, times(1)).save(OrderHistory.ADD_PAYMENT_MANUALLY + 1,
            "Петро  Петренко", order);
        verify(eventService, times(1))
            .save(OrderHistory.ORDER_HALF_PAID, OrderHistory.SYSTEM, order);
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void saveNewManualPaymentWithPaidAmount() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = getFormedOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        Payment payment = getManualPayment();
        payment.setAmount(500_00L);
        order.setPayment(singletonList(payment));
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementdate("02-08-2021").amount(500_00L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBag1list());
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(any(), any(), any());
        ubsManagementService.saveNewManualPayment(1L, paymentDetails, null, "test@gmail.com");
        verify(employeeRepository, times(2)).findByEmail(anyString());
        verify(eventService, times(1)).save(OrderHistory.ADD_PAYMENT_MANUALLY + 1,
            "Петро  Петренко", order);
        verify(eventService, times(1))
            .save(OrderHistory.ORDER_PAID, OrderHistory.SYSTEM, order);
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void saveNewManualPaymentWithPaidOrder() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = getFormedOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        Payment payment = getManualPayment();
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementdate("02-08-2021").amount(500L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(OrderHistory.ADD_PAYMENT_MANUALLY + 1, "Петро" + "  " + "Петренко", order);
        ubsManagementService.saveNewManualPayment(1L, paymentDetails, null, "test@gmail.com");
        verify(eventService, times(1))
            .save("Додано оплату №1", "Петро  Петренко", order);
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void saveNewManualPaymentWithPartiallyPaidOrder() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = getFormedOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        order.setOrderPaymentStatus(OrderPaymentStatus.HALF_PAID);
        Payment payment = getManualPayment();
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementdate("02-08-2021").amount(200L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(OrderHistory.ADD_PAYMENT_MANUALLY + 1, "Петро" + "  " + "Петренко", order);
        ubsManagementService.saveNewManualPayment(1L, paymentDetails, null, "test@gmail.com");
        verify(eventService, times(1))
            .save("Додано оплату №1", "Петро  Петренко", order);
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void saveNewManualPaymentWithUnpaidOrder() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = getFormedOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        order.setOrderPaymentStatus(OrderPaymentStatus.UNPAID);
        Payment payment = getManualPayment();
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementdate("02-08-2021").amount(500L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(OrderHistory.ADD_PAYMENT_MANUALLY + 1, "Петро" + "  " + "Петренко", order);
        ubsManagementService.saveNewManualPayment(1L, paymentDetails, null, "test@gmail.com");
        verify(eventService, times(1))
            .save("Додано оплату №1", "Петро  Петренко", order);
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void saveNewManualPaymentWithPaymentRefundedOrder() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = getFormedOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAYMENT_REFUNDED);
        Payment payment = getManualPayment();
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementdate("02-08-2021").amount(500L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(OrderHistory.ADD_PAYMENT_MANUALLY + 1, "Петро" + "  " + "Петренко", order);
        ubsManagementService.saveNewManualPayment(1L, paymentDetails, null, "test@gmail.com");
        verify(eventService, times(1))
            .save("Додано оплату №1", "Петро  Петренко", order);
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void checkUpdateManualPaymentWithImage() {
        Employee employee = getEmployee();
        Order order = ModelUtils.getFormedHalfPaidOrder();
        employee.setFirstName("Yuriy");
        employee.setLastName("Gerasum");
        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(employee));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        MockMultipartFile file = new MockMultipartFile("manualPaymentDto",
            "", "application/json", "random Bytes".getBytes());
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(getManualPayment()));
        when(paymentRepository.save(any())).thenReturn(getManualPayment());
        when(fileService.upload(file)).thenReturn("path");
        doNothing().when(eventService).save(OrderHistory.UPDATE_PAYMENT_MANUALLY + 1, "Yuriy" + "  " + "Gerasum",
            getOrder());
        ubsManagementService.updateManualPayment(1L, getManualPaymentRequestDto(), file, "abc");
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(any());
        verify(eventService, times(2)).save(any(), any(), any());
    }

    @Test
    void checkManualPaymentNotFound() {
        Employee employee = getEmployee();
        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(employee));
        ManualPaymentRequestDto manualPaymentRequestDto = getManualPaymentRequestDto();
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.updateManualPayment(1L, manualPaymentRequestDto, null, "abc"));
        verify(paymentRepository, times(1)).findById(1L);
    }

    @Test
    void checkReturnOverpaymentInfo() {
        Order order = getOrder();
        when(orderRepository.findUserById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        order.setUser(ModelUtils.getUser());

        Double sumToPay = 0.;

        assertEquals("Зарахування на бонусний рахунок", ubsManagementService.returnOverpaymentInfo(1L, sumToPay, 0L)
            .getPaymentInfoDtos().get(1).getComment());

        assertEquals(0L, ubsManagementService.returnOverpaymentInfo(1L, sumToPay, 1L)
            .getOverpayment());
        assertEquals(AppConstant.PAYMENT_REFUND,
            ubsManagementService.returnOverpaymentInfo(order.getId(), sumToPay, 1L).getPaymentInfoDtos().get(1)
                .getComment());
    }

    @Test
    void checkReturnOverpaymentThroweException() {
        Assertions.assertThrows(NotFoundException.class,
            () -> ubsManagementService.returnOverpaymentInfo(100L, 1., 1L));
    }

    @Test
    void checkReturnOverpaymentThroweExceptioninGetPaymentInfo() {
        Order order = getOrder();
        when(orderRepository.findUserById(1L)).thenReturn(Optional.of(order));

        Assertions.assertThrows(NotFoundException.class, () -> ubsManagementService.returnOverpaymentInfo(1L, 1., 1L));
    }

    @Test
    void checkGetPaymentInfo() {
        Order order = getOrder();
        order.setOrderStatus(OrderStatus.DONE);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        assertEquals(100L, ubsManagementService.getPaymentInfo(order.getId(), 800.).getOverpayment());
        assertEquals(200L, ubsManagementService.getPaymentInfo(order.getId(), 100.).getPaidAmount());
        assertEquals(0L, ubsManagementService.getPaymentInfo(order.getId(), 100.).getUnPaidAmount());
        verify(orderRepository, times(3)).findById(order.getId());
    }

    @Test
    void checkGetPaymentInfoIfOrderStatusIsCanceled() {
        Order order = getOrder();
        order.setOrderStatus(OrderStatus.CANCELED);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        assertEquals(200L, ubsManagementService.getPaymentInfo(order.getId(), 800.).getOverpayment());
        assertEquals(200L, ubsManagementService.getPaymentInfo(order.getId(), 100.).getPaidAmount());
        assertEquals(0L, ubsManagementService.getPaymentInfo(order.getId(), 100.).getUnPaidAmount());
        verify(orderRepository, times(3)).findById(order.getId());
    }

    @Test
    void checkGetPaymentInfoIfSumToPayIsNull() {
        Order order = getOrder();
        order.setOrderStatus(OrderStatus.DONE);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        assertEquals(900L, ubsManagementService.getPaymentInfo(order.getId(), null).getOverpayment());
        verify(orderRepository).findById(order.getId());
    }

    @Test
    void updateOrderDetailStatusThrowException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrder()));
        OrderDetailStatusRequestDto requestDto = getTestOrderDetailStatusRequestDto();
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.updateOrderDetailStatus(1L, requestDto, "uuid"));
    }

    @Test
    void updateOrderDetailStatusFirst() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = user.getOrders().get(0);
        order.setOrderDate((LocalDateTime.of(2021, 5, 15, 10, 20, 5)));

        List<Payment> payment = new ArrayList<>();
        payment.add(Payment.builder().build());

        order.setPayment(payment);

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(paymentRepository.findAllByOrderId(anyLong())).thenReturn(payment);
        when(paymentRepository.saveAll(any())).thenReturn(payment);
        when(orderRepository.save(any())).thenReturn(order);

        OrderDetailStatusRequestDto testOrderDetail = getTestOrderDetailStatusRequestDto();
        OrderDetailStatusDto expectedObject = ModelUtils.getTestOrderDetailStatusDto();
        OrderDetailStatusDto producedObject = ubsManagementService
            .updateOrderDetailStatus(order.getId(), testOrderDetail, "test@gmail.com");
        assertEquals(expectedObject.getOrderStatus(), producedObject.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObject.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObject.getDate());
        testOrderDetail.setOrderStatus(OrderStatus.FORMED.toString());
        expectedObject.setOrderStatus(OrderStatus.FORMED.toString());
        OrderDetailStatusDto producedObjectAdjustment = ubsManagementService
            .updateOrderDetailStatus(order.getId(), testOrderDetail, "test@gmail.com");

        assertEquals(expectedObject.getOrderStatus(), producedObjectAdjustment.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectAdjustment.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectAdjustment.getDate());

        testOrderDetail.setOrderStatus(OrderStatus.ADJUSTMENT.toString());
        expectedObject.setOrderStatus(OrderStatus.ADJUSTMENT.toString());
        OrderDetailStatusDto producedObjectConfirmed = ubsManagementService
            .updateOrderDetailStatus(order.getId(), testOrderDetail, "test@gmail.com");

        assertEquals(expectedObject.getOrderStatus(), producedObjectConfirmed.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectConfirmed.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectConfirmed.getDate());

        testOrderDetail.setOrderStatus(OrderStatus.CONFIRMED.toString());
        expectedObject.setOrderStatus(OrderStatus.CONFIRMED.toString());
        OrderDetailStatusDto producedObjectNotTakenOut = ubsManagementService
            .updateOrderDetailStatus(order.getId(), testOrderDetail, "test@gmail.com");

        assertEquals(expectedObject.getOrderStatus(), producedObjectNotTakenOut.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectNotTakenOut.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectNotTakenOut.getDate());

        testOrderDetail.setCancellationReason("MOVING_OUT");
        testOrderDetail.setOrderStatus(OrderStatus.CANCELED.toString());
        expectedObject.setOrderStatus(OrderStatus.CANCELED.toString());
        OrderDetailStatusDto producedObjectCancelled = ubsManagementService
            .updateOrderDetailStatus(order.getId(), testOrderDetail, "test@gmail.com");

        assertEquals(expectedObject.getOrderStatus(), producedObjectCancelled.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectCancelled.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectCancelled.getDate());
        assertEquals(0, order.getPointsToUse());

        order.setOrderStatus(OrderStatus.ON_THE_ROUTE);
        testOrderDetail.setOrderStatus(OrderStatus.DONE.toString());
        expectedObject.setOrderStatus(OrderStatus.DONE.toString());
        OrderDetailStatusDto producedObjectDone = ubsManagementService
            .updateOrderDetailStatus(order.getId(), testOrderDetail, "test@gmail.com");

        assertEquals(expectedObject.getOrderStatus(), producedObjectDone.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectDone.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectDone.getDate());

        order.setPointsToUse(0);
        order.setOrderStatus(OrderStatus.ON_THE_ROUTE);
        testOrderDetail.setOrderStatus(OrderStatus.CANCELED.toString());
        expectedObject.setOrderStatus(OrderStatus.CANCELED.toString());
        OrderDetailStatusDto producedObjectCancelled2 = ubsManagementService
            .updateOrderDetailStatus(order.getId(), testOrderDetail, "test@gmail.com");

        assertEquals(expectedObject.getOrderStatus(), producedObjectCancelled2.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectCancelled2.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectCancelled2.getDate());
        assertEquals(0, order.getPointsToUse());

        order.setOrderStatus(OrderStatus.ADJUSTMENT);
        testOrderDetail.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF.toString());
        expectedObject.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF.toString());
        OrderDetailStatusDto producedObjectBroughtItHimself = ubsManagementService
            .updateOrderDetailStatus(order.getId(), testOrderDetail, "test@gmail.com");

        assertEquals(expectedObject.getOrderStatus(), producedObjectBroughtItHimself.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectBroughtItHimself.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectBroughtItHimself.getDate());

        order.setCertificates(Set.of(ModelUtils.getCertificate()));
        testOrderDetail.setOrderStatus(OrderStatus.CANCELED.toString());
        expectedObject.setOrderStatus(OrderStatus.CANCELED.toString());
        OrderDetailStatusDto producedObjectCancelled3 = ubsManagementService
            .updateOrderDetailStatus(order.getId(), testOrderDetail, "test@gmail.com");

        assertEquals(expectedObject.getOrderStatus(), producedObjectCancelled3.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectCancelled3.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectCancelled3.getDate());
        assertEquals(0, order.getPointsToUse());

        verify(eventService, times(1))
            .saveEvent("Статус Замовлення - Узгодження",
                "test@gmail.com", order);
        verify(eventService, times(1))
            .saveEvent("Статус Замовлення - Підтверджено",
                "test@gmail.com", order);
        verify(eventService, times(3))
            .saveEvent("Статус Замовлення - Скасовано",
                "test@gmail.com", order);
        verify(eventService, times(1))
            .saveEvent("Невикористані бонуси повернено на бонусний рахунок клієнта" + ". Всього " + 700,
                "test@gmail.com", order);
    }

    @Test
    void updateOrderDetailStatusSecond() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = user.getOrders().get(0);
        order.setOrderDate((LocalDateTime.now()));

        List<Payment> payment = new ArrayList<>();
        payment.add(Payment.builder().build());

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(paymentRepository.findAllByOrderId(anyLong())).thenReturn(payment);
        when(paymentRepository.saveAll(any())).thenReturn(payment);
        when(orderRepository.save(any())).thenReturn(order);

        OrderDetailStatusRequestDto testOrderDetail = getTestOrderDetailStatusRequestDto();
        OrderDetailStatusDto expectedObject = ModelUtils.getTestOrderDetailStatusDto();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        expectedObject.setDate(LocalDateTime.now().format(formatter));
        testOrderDetail.setOrderStatus(OrderStatus.ON_THE_ROUTE.toString());
        expectedObject.setOrderStatus(OrderStatus.ON_THE_ROUTE.toString());
        OrderDetailStatusDto producedObjectOnTheRoute = ubsManagementService
            .updateOrderDetailStatus(order.getId(), testOrderDetail, "abc");

        assertEquals(expectedObject.getOrderStatus(), producedObjectOnTheRoute.getOrderStatus());
        assertEquals(expectedObject.getPaymentStatus(), producedObjectOnTheRoute.getPaymentStatus());
        assertEquals(expectedObject.getDate(), producedObjectOnTheRoute.getDate());
    }

    @Test
    void getAllEmployeesByPosition() {
        Order order = getOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        EmployeePositionDtoRequest dto = ModelUtils.getEmployeePositionDtoRequest();
        List<EmployeeOrderPosition> newList = new ArrayList<>();
        newList.add(ModelUtils.getEmployeeOrderPosition());
        List<Position> positionList = new ArrayList<>();
        positionList.add(ModelUtils.getPosition());
        List<Employee> employeeList = new ArrayList<>();
        employeeList.add(getEmployee());
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(employeeOrderPositionRepository.findAllByOrderId(anyLong())).thenReturn(newList);
        when(positionRepository.findAll()).thenReturn(positionList);
        when(employeeRepository.findAllByEmployeePositionId(anyLong())).thenReturn(employeeList);
        assertEquals(dto, ubsManagementService.getAllEmployeesByPosition(1L, "test@gmail.com"));
        verify(orderRepository).findById(anyLong());
        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(employeeOrderPositionRepository).findAllByOrderId(anyLong());
        verify(positionRepository).findAll();
        verify(employeeRepository).findAllByEmployeePositionId(anyLong());
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void getAllEmployeesByPositionThrowBadRequestException() {
        Order order = getOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.empty());
        assertThrows(BadRequestException.class,
            () -> ubsManagementService.getAllEmployeesByPosition(1L, "test@gmail.com"));
        verify(orderRepository).findById(anyLong());
        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void testUpdateAddress() {
        OrderAddress orderAddress = getOrderAddress();
        orderAddress.setId(1L);
        OrderAddressExportDetailsDtoUpdate dtoUpdate = ModelUtils.getOrderAddressExportDetailsDtoUpdate();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(TEST_ORDER));
        when(orderAddressRepository.findById(dtoUpdate.getAddressId())).thenReturn(Optional.of(orderAddress));

        when(orderAddressRepository.save(orderAddress)).thenReturn(orderAddress);
        when(modelMapper.map(orderAddress, OrderAddressDtoResponse.class)).thenReturn(TEST_ORDER_ADDRESS_DTO_RESPONSE);
        Optional<OrderAddressDtoResponse> actual =
            ubsManagementService.updateAddress(TEST_ORDER_ADDRESS_DTO_UPDATE, 1L, "test@gmail.com");
        assertEquals(Optional.of(TEST_ORDER_ADDRESS_DTO_RESPONSE), actual);
        verify(orderRepository).findById(1L);
        verify(orderAddressRepository).save(orderAddress);
        verify(orderAddressRepository).findById(orderAddress.getId());
        verify(modelMapper).map(orderAddress, OrderAddressDtoResponse.class);
    }

    @Test
    void testUpdateAddressThrowsOrderNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> ubsManagementService.updateAddress(TEST_ORDER_ADDRESS_DTO_UPDATE, 1L, "abc"));
    }

    @Test
    void testUpdateAddressThrowsNotFoundOrderAddressException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getOrderWithoutAddress()));

        assertThrows(NotFoundException.class,
            () -> ubsManagementService.updateAddress(TEST_ORDER_ADDRESS_DTO_UPDATE, 1L, "abc"));
    }

    @Test
    void testGetOrderDetailStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(TEST_ORDER));
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(TEST_PAYMENT_LIST);

        OrderDetailStatusDto actual = ubsManagementService.getOrderDetailStatus(1L);

        assertEquals(ORDER_DETAIL_STATUS_DTO.getOrderStatus(), actual.getOrderStatus());
        assertEquals(ORDER_DETAIL_STATUS_DTO.getPaymentStatus(), actual.getPaymentStatus());
        assertEquals(ORDER_DETAIL_STATUS_DTO.getDate(), actual.getDate());

        verify(orderRepository).findById(1L);
        verify(paymentRepository).findAllByOrderId(1L);
    }

    @Test
    void testGetOrderDetailStatusThrowsUnExistingOrderException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> ubsManagementService.getOrderDetailStatus(1L));
    }

    @Test
    void testGetOrderDetailStatusThrowsPaymentNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(TEST_ORDER));
        when(paymentRepository.findAllByOrderId(1)).thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class,
            () -> ubsManagementService.getOrderDetailStatus(1L));
    }

    @Test
    void testGetOrderDetails() {
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(TEST_ORDER));
        when(modelMapper.map(TEST_ORDER, new TypeToken<List<BagMappingDto>>() {
        }.getType())).thenReturn(TEST_BAG_MAPPING_DTO_LIST);
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(TEST_BAG_LIST);
        when(modelMapper.map(TEST_BAG, BagInfoDto.class)).thenReturn(TEST_BAG_INFO_DTO);
        when(bagRepository.findAllByOrder(1L)).thenReturn(TEST_BAG_LIST);
        when(modelMapper.map(any(), eq(new TypeToken<List<OrderDetailInfoDto>>() {
        }.getType()))).thenReturn(TEST_ORDER_DETAILS_INFO_DTO_LIST);

        List<OrderDetailInfoDto> actual = ubsManagementService.getOrderDetails(1L, "ua");

        assertEquals(TEST_ORDER_DETAILS_INFO_DTO_LIST, actual);

        verify(orderRepository).getOrderDetails(1L);
        verify(modelMapper).map(TEST_ORDER, new TypeToken<List<BagMappingDto>>() {
        }.getType());
        verify(bagRepository).findBagsByOrderId(1L);
        verify(bagRepository, times(1)).findAllByOrder(anyLong());
        verify(modelMapper).map(TEST_BAG, BagInfoDto.class);
        verify(modelMapper).map(any(), eq(new TypeToken<List<OrderDetailInfoDto>>() {
        }.getType()));
    }

    @Test
    void testGetOrdersBagsDetails() {
        List<DetailsOrderInfoDto> detailsOrderInfoDtoList = new ArrayList<>();
        Map<String, Object> mapOne = Map.of("One", "Two");
        Map<String, Object> mapTwo = Map.of("One", "Two");
        List<Map<String, Object>> mockBagInfoRepository = Arrays.asList(mapOne, mapTwo);
        when(bagRepository.getBagInfo(1L)).thenReturn(mockBagInfoRepository);
        for (Map<String, Object> map : mockBagInfoRepository) {
            when(objectMapper.convertValue(map, DetailsOrderInfoDto.class)).thenReturn(getTestDetailsOrderInfoDto());
            detailsOrderInfoDtoList.add(getTestDetailsOrderInfoDto());
        }
        assertEquals(detailsOrderInfoDtoList.toString(),
            ubsManagementService.getOrderBagsDetails(1L).toString());
    }

    @Test
    void testGetOrderExportDetailsReceivingStationNotFoundExceptionThrown() {
        when(orderRepository.findById(1L))
            .thenReturn(Optional.of(getOrder()));
        List<ReceivingStation> receivingStations = new ArrayList<>();
        when(receivingStationRepository.findAll())
            .thenReturn(receivingStations);
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.getOrderExportDetails(1L));
    }

    @Test
    void testGetOrderDetailsThrowsException() {
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> ubsManagementService.getOrderDetails(1L, "ua"));
    }

    @Test
    void checkGetAllUserViolations() {
        User user = ModelUtils.getUser();
        user.setUuid(userRemoteClient.findUuidByEmail(user.getRecipientEmail()));
        user.setViolations(1);

        ViolationsInfoDto expected = modelMapper.map(user, ViolationsInfoDto.class);

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));

        ViolationsInfoDto actual = ubsManagementService.getAllUserViolations(user.getRecipientEmail());

        assertEquals(expected, actual);
    }

    @Test
    void testAddPointToUserThrowsException() {
        User user = getTestUser();
        user.setUuid(null);

        AddingPointsToUserDto addingPointsToUserDto =
            AddingPointsToUserDto.builder().additionalPoints(anyInt()).build();
        assertThrows(NotFoundException.class, () -> ubsManagementService.addPointsToUser(addingPointsToUserDto));
    }

    @Test
    void testAddPointsToUser() {
        User user = getTestUser();
        user.setUuid(userRemoteClient.findUuidByEmail(user.getRecipientEmail()));
        user.setCurrentPoints(1);

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        ubsManagementService.addPointsToUser(AddingPointsToUserDto.builder().additionalPoints(anyInt()).build());

        assertEquals(2L, user.getChangeOfPointsList().size());
    }

    @Test
    void testGetAdditionalBagsInfo() {
        when(userRepository.findUserByOrderId(1L)).thenReturn(Optional.of(TEST_USER));
        when(bagRepository.getAdditionalBagInfo(1L, TEST_USER.getRecipientEmail()))
            .thenReturn(TEST_MAP_ADDITIONAL_BAG_LIST);
        when(objectMapper.convertValue(any(), eq(AdditionalBagInfoDto.class)))
            .thenReturn(TEST_ADDITIONAL_BAG_INFO_DTO);

        List<AdditionalBagInfoDto> actual = ubsManagementService.getAdditionalBagsInfo(1L);

        assertEquals(TEST_ADDITIONAL_BAG_INFO_DTO_LIST, actual);

        verify(userRepository).findUserByOrderId(1L);
        verify(bagRepository).getAdditionalBagInfo(1L, TEST_USER.getRecipientEmail());
        verify(objectMapper).convertValue(any(), eq(AdditionalBagInfoDto.class));
    }

    @Test
    void testGetAdditionalBagsInfoThrowsException() {
        when(userRepository.findUserByOrderId(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> ubsManagementService.getAdditionalBagsInfo(1L));
    }

    @Test
    void testSetOrderDetail() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusDoneDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(bagRepository.findById(1)).thenReturn(Optional.of(ModelUtils.getTariffBag()));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBag1list());
        when(paymentRepository.selectSumPaid(1L)).thenReturn(5000L);

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "abc");

        verify(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailNeedToChangeStatusToHALF_PAID() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusDoneDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(bagRepository.findById(1)).thenReturn(Optional.of(ModelUtils.getTariffBag()));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBag1list());
        when(paymentRepository.selectSumPaid(1L)).thenReturn(5000L);
        doNothing().when(orderRepository).updateOrderPaymentStatus(1L, OrderPaymentStatus.HALF_PAID.name());

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "abc");

        verify(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        verify(orderRepository).updateOrderPaymentStatus(1L, OrderPaymentStatus.HALF_PAID.name());
        verify(notificationService).notifyHalfPaidPackage(any(Order.class));
    }

    @Test
    void testSetOrderDetailNeedToChangeStatusToUNPAID() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusDoneDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(bagRepository.findById(1)).thenReturn(Optional.of(ModelUtils.getTariffBag()));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBag1list());
        when(paymentRepository.selectSumPaid(1L)).thenReturn(null);
        doNothing().when(orderRepository).updateOrderPaymentStatus(1L, OrderPaymentStatus.UNPAID.name());

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "abc");

        verify(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        verify(orderRepository).updateOrderPaymentStatus(1L, OrderPaymentStatus.UNPAID.name());
    }

    @Test
    void testSetOrderDetailWhenSumPaidIsNull() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusDoneDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(bagRepository.findById(1)).thenReturn(Optional.of(ModelUtils.getTariffBag()));
        when(paymentRepository.selectSumPaid(1L)).thenReturn(null);

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "abc");

        verify(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailWhenOrderNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        Map<Integer, Integer> amountOfBagsConfirmed = UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto()
            .getAmountOfBagsConfirmed();
        Map<Integer, Integer> amountOfBagsExported = UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto()
            .getAmountOfBagsExported();

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> ubsManagementService.setOrderDetail(
                1L,
                amountOfBagsConfirmed,
                amountOfBagsExported,
                "abc"),
            ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST);

        assertEquals(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST, exception.getMessage());

        verify(orderRepository).findById(1L);
    }

    @Test
    void testSetOrderDetailIfHalfPaid() {
        Order orderDto = ModelUtils.getOrdersStatusDoneDto();
        Order orderDetailDto = getOrdersStatusFormedDto();
        Bag tariffBagDto = ModelUtils.getTariffBag();
        List<Bag> bagList = getBaglist();
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(orderDto));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(orderDetailDto));
        when(bagRepository.findById(1)).thenReturn(Optional.ofNullable(tariffBagDto));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(bagList);
        when(paymentRepository.selectSumPaid(1L)).thenReturn(0L);
        when(orderRepository.findSumOfCertificatesByOrderId(1L)).thenReturn(0L);

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "abc");
        verify(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        verify(orderRepository).updateOrderPaymentStatus(1L, OrderPaymentStatus.UNPAID.name());
    }

    @Test
    void testSetOrderDetailIfPaidAndPriceLessThanDiscount() {
        Order order = ModelUtils.getOrdersStatusAdjustmentDto2();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(certificateRepository.findCertificate(order.getId())).thenReturn(List.of(ModelUtils.getCertificate2()));
        when(orderRepository.findSumOfCertificatesByOrderId(order.getId())).thenReturn(600L);
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusFormedDto2()));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBaglist());
        when(bagRepository.findById(1)).thenReturn(Optional.of(ModelUtils.getTariffBag()));

        ubsManagementService.setOrderDetail(order.getId(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "abc");

        verify(certificateRepository).save(ModelUtils.getCertificate2().setPoints(20));
        verify(userRepository).updateUserCurrentPoints(1L, 100);
        verify(orderRepository).updateOrderPointsToUse(1L, 0);
    }

    @Test
    void testSetOrderDetailIfPaidAndPriceLessThanPaidSum() {
        Order order = ModelUtils.getOrdersStatusAdjustmentDto2();
        when(paymentRepository.selectSumPaid(order.getId())).thenReturn(10000L);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(certificateRepository.findCertificate(order.getId())).thenReturn(List.of(ModelUtils.getCertificate2()));
        when(orderRepository.findSumOfCertificatesByOrderId(order.getId())).thenReturn(600L);
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusFormedDto2()));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBaglist());
        when(bagRepository.findById(1)).thenReturn(Optional.of(ModelUtils.getTariffBag()));

        ubsManagementService.setOrderDetail(order.getId(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "abc");

        verify(certificateRepository).save(ModelUtils.getCertificate2().setPoints(0));
        verify(userRepository).updateUserCurrentPoints(1L, 100);
        verify(orderRepository).updateOrderPointsToUse(1L, 0);
    }

    @Test
    void testSetOrderDetailConfirmed() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusConfirmedDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(bagRepository.findById(1)).thenReturn(Optional.of(ModelUtils.getTariffBag()));
        when(bagRepository.findById(1)).thenReturn(Optional.of(ModelUtils.getTariffBag()));
        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "test@gmail.com");

        verify(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        verify(orderDetailRepository, times(0)).updateExporter(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailConfirmed2() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusConfirmedDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(bagRepository.findById(1)).thenReturn(Optional.of(ModelUtils.getTariffBag()));
        when(orderDetailRepository.ifRecordExist(any(), any())).thenReturn(1L);
        when(orderDetailRepository.getAmount(any(), any())).thenReturn(1L);
        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "test@gmail.com");
        verify(orderRepository, times(2)).findById(1L);
        verify(bagRepository, times(2)).findCapacityById(1);
        verify(bagRepository, times(2)).findById(1);
        verify(orderDetailRepository, times(2)).ifRecordExist(any(), any());
        verify(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        verify(orderDetailRepository).getAmount(any(), any());
        verify(orderDetailRepository, times(0)).updateExporter(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailWithExportedWaste() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusDoneDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(bagRepository.findById(1)).thenReturn(Optional.of(ModelUtils.getTariffBag()));
        when(orderDetailRepository.ifRecordExist(any(), any())).thenReturn(1L);
        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "test@gmail.com");
        verify(orderRepository, times(2)).findById(1L);
        verify(bagRepository, times(2)).findCapacityById(1);
        verify(bagRepository, times(2)).findById(1);
        verify(orderDetailRepository, times(3)).ifRecordExist(any(), any());
        verify(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailFormed() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(bagRepository.findById(1)).thenReturn(Optional.of(ModelUtils.getTariffBag()));

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "test@gmail.com");

        verify(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailFormedWithBagNoPresent() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(bagRepository.findById(1)).thenReturn(Optional.empty());

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(), "test@gmail.com");

        verify(orderDetailRepository, times(0)).updateExporter(anyInt(), anyLong(), anyLong());
        verify(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());

    }

    @Test
    void testSetOrderDetailNotTakenOut() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusNotTakenOutDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(bagRepository.findById(1)).thenReturn(Optional.of(ModelUtils.getTariffBag()));
        when(bagRepository.findById(1)).thenReturn(Optional.of(ModelUtils.getTariffBag()));

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "abc");

        verify(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailOnTheRoute() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusOnThe_RouteDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(bagRepository.findById(1)).thenReturn(Optional.of(ModelUtils.getTariffBag()));

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "abc");

        verify(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        verify(orderDetailRepository, times(0)).updateExporter(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailsDone() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusDoneDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(bagRepository.findById(1)).thenReturn(Optional.of(ModelUtils.getTariffBag()));
        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "abc");

        verify(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        verify(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderBroughtItHimself() {
        when(orderRepository.findById(1L))
            .thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusBROUGHT_IT_HIMSELFDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(bagRepository.findById(1)).thenReturn(Optional.of(ModelUtils.getTariffBag()));
        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "abc");

        verify(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        verify(orderDetailRepository, times(0)).updateExporter(anyInt(), anyLong(), anyLong());
    }

    @Test
    void testSetOrderDetailsCanseled() {
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getOrdersStatusCanseledDto()));
        when(bagRepository.findCapacityById(1)).thenReturn(1);
        doNothing().when(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
        doNothing().when(orderDetailRepository).updateConfirm(anyInt(), anyLong(), anyLong());
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(bagRepository.findById(1)).thenReturn(Optional.of(ModelUtils.getTariffBag()));

        ubsManagementService.setOrderDetail(1L,
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed(),
            UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported(),
            "abc");

        verify(orderDetailRepository).updateExporter(anyInt(), anyLong(), anyLong());
    }

    @Test
    void setOrderDetailExceptionTest() {
        Map<Integer, Integer> confirm = UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed();
        Map<Integer, Integer> exported = UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported();

        assertThrows(NotFoundException.class,
            () -> ubsManagementService.setOrderDetail(1L, confirm, exported, "test@gmail.com"));
    }

    @Test
    void testSetOrderDetailThrowsUserNotFoundException() {
        Map<Integer, Integer> confirm = UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsConfirmed();
        Map<Integer, Integer> exported = UPDATE_ORDER_PAGE_ADMIN_DTO.getOrderDetailDto().getAmountOfBagsExported();

        assertThrows(NotFoundException.class,
            () -> ubsManagementService.setOrderDetail(1L, confirm, exported, "test@gmail.com"));
    }

    @Test
    void testSaveAdminToOrder() {
        Order order = getOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        ubsManagementService.saveAdminCommentToOrder(getAdminCommentDto(), "test@gmail.com");
        verify(orderRepository, times(1)).save(order);
        verify(eventService, times(1)).save(any(), any(), any());
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void testUpdateEcoNumberForOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(getOrder()));
        ubsManagementService.updateEcoNumberForOrder(getEcoNumberDto(), 1L, "abc");
        verify(eventService, times(1)).saveEvent(any(), any(), any());
    }

    @Test
    void testUpdateEcoNumberThrowOrderNotFoundException() {
        EcoNumberDto dto = getEcoNumberDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.updateEcoNumberForOrder(dto, 1L, "test@gmail.com"));
    }

    @Test
    void updateEcoNumberTrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        EcoNumberDto ecoNumberDto = getEcoNumberDto();
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.updateEcoNumberForOrder(ecoNumberDto, 1L, "abc"));
    }

    @Test
    void updateEcoNumberTrowsIncorrectEcoNumberFormatException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(getOrder()));

        EcoNumberDto ecoNumberDto = getEcoNumberDto();
        ecoNumberDto.setEcoNumber(new HashSet<>(List.of("1234a")));
        assertThrows(BadRequestException.class,
            () -> ubsManagementService.updateEcoNumberForOrder(ecoNumberDto, 1L, "abc"));
    }

    @Test
    void saveAdminCommentThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        AdminCommentDto adminCommentDto = getAdminCommentDto();
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.saveAdminCommentToOrder(adminCommentDto, "abc"));
    }

    @Test
    void getOrdersForUserTest() {
        Order order1 = getOrderUserFirst();
        Order order2 = ModelUtils.getOrderUserSecond();
        List<Order> orders = List.of(order1, order2);
        OrderInfoDto info1 = OrderInfoDto.builder().id(1L).orderPrice(10).build();
        OrderInfoDto info2 = OrderInfoDto.builder().id(2L).orderPrice(20).build();
        when(orderRepository.getAllOrdersOfUser("uuid")).thenReturn(orders);
        when(modelMapper.map(order1, OrderInfoDto.class)).thenReturn(info1);
        when(modelMapper.map(order2, OrderInfoDto.class)).thenReturn(info2);
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order1));
        when(orderRepository.getOrderDetails(2L)).thenReturn(Optional.of(order2));

        ubsManagementService.getOrdersForUser("uuid");

        verify(orderRepository).getAllOrdersOfUser("uuid");
        verify(modelMapper).map(order1, OrderInfoDto.class);
        verify(modelMapper).map(order2, OrderInfoDto.class);
        verify(orderRepository).getOrderDetails(1L);
        verify(orderRepository).getOrderDetails(2L);
    }

    @Test
    void getOrderStatusDataThrowsUnexistingOrderExceptionTest() {
        Assertions.assertThrows(NotFoundException.class, () -> ubsManagementService.getOrderStatusData(100L, null));
    }

    @Test
    void updateOrderAdminPageInfoTest() {
        OrderDetailStatusRequestDto orderDetailStatusRequestDto = getTestOrderDetailStatusRequestDto();
        Order order = getOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setOrderDate(LocalDateTime.now()).setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(paymentRepository.findAllByOrderId(1L))
            .thenReturn(List.of(getPayment()));
        lenient().when(ubsManagementServiceMock.updateOrderDetailStatus(1L, orderDetailStatusRequestDto, "abc"))
            .thenReturn(ModelUtils.getTestOrderDetailStatusDto());
        when(ubsClientService.updateUbsUserInfoInOrder(ModelUtils.getUbsCustomersDtoUpdate(),
            "test@gmail.com")).thenReturn(ModelUtils.getUbsCustomersDto());
        UpdateOrderPageAdminDto updateOrderPageAdminDto = updateOrderPageAdminDto();
        updateOrderPageAdminDto.setUserInfoDto(ModelUtils.getUbsCustomersDtoUpdate());
        when(orderAddressRepository.findById(1L))
            .thenReturn(Optional.of(getOrderAddress()));
        when(receivingStationRepository.findAll())
            .thenReturn(List.of(getReceivingStation()));
        var receivingStation = getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(bagRepository.findById(1)).thenReturn(Optional.of(ModelUtils.getTariffBag()));

        ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, 1L, "en", "test@gmail.com");
        UpdateOrderPageAdminDto emptyDto = new UpdateOrderPageAdminDto();
        ubsManagementService.updateOrderAdminPageInfo(emptyDto, 1L, "en", "test@gmail.com");

        verify(ubsClientService, times(1))
            .updateUbsUserInfoInOrder(ModelUtils.getUbsCustomersDtoUpdate(), "test@gmail.com");
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void updateOrderAdminPageInfoWithUbsCourierSumAndWriteOffStationSum() {
        OrderDetailStatusRequestDto orderDetailStatusRequestDto = getTestOrderDetailStatusRequestDto();
        Order order = getOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setOrderDate(LocalDateTime.now()).setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(paymentRepository.findAllByOrderId(1L))
            .thenReturn(List.of(getPayment()));
        lenient().when(ubsManagementServiceMock.updateOrderDetailStatus(1L, orderDetailStatusRequestDto, "abc"))
            .thenReturn(ModelUtils.getTestOrderDetailStatusDto());
        when(ubsClientService.updateUbsUserInfoInOrder(ModelUtils.getUbsCustomersDtoUpdate(),
            "test@gmail.com")).thenReturn(ModelUtils.getUbsCustomersDto());
        UpdateOrderPageAdminDto updateOrderPageAdminDto = updateOrderPageAdminDto();
        updateOrderPageAdminDto.setUserInfoDto(ModelUtils.getUbsCustomersDtoUpdate());
        updateOrderPageAdminDto.setUbsCourierSum(50.);
        updateOrderPageAdminDto.setWriteOffStationSum(100.);
        when(orderAddressRepository.findById(1L))
            .thenReturn(Optional.of(getOrderAddress()));
        when(receivingStationRepository.findAll())
            .thenReturn(List.of(getReceivingStation()));
        var receivingStation = getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(orderRepository.getOrderDetails(anyLong()))
            .thenReturn(Optional.ofNullable(getOrdersStatusFormedDto()));
        when(bagRepository.findById(1)).thenReturn(Optional.of(ModelUtils.getTariffBag()));

        ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, 1L, "en", "test@gmail.com");
        UpdateOrderPageAdminDto emptyDto = new UpdateOrderPageAdminDto();
        ubsManagementService.updateOrderAdminPageInfo(emptyDto, 1L, "en", "test@gmail.com");

        verify(ubsClientService, times(1))
            .updateUbsUserInfoInOrder(ModelUtils.getUbsCustomersDtoUpdate(), "test@gmail.com");
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void updateOrderAdminPageInfoWithStatusFormedTest() {
        Order order = getOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setOrderDate(LocalDateTime.now()).setTariffsInfo(tariffsInfo);
        order.setOrderStatus(OrderStatus.FORMED);
        EmployeeOrderPosition employeeOrderPosition = ModelUtils.getEmployeeOrderPosition();
        Employee employee = getEmployee();
        UpdateOrderPageAdminDto updateOrderPageAdminDto = ModelUtils.updateOrderPageAdminDtoWithStatusFormed();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(getPayment()));
        when(receivingStationRepository.findAll()).thenReturn(List.of(getReceivingStation()));
        when(employeeOrderPositionRepository.findAllByOrderId(1L)).thenReturn(List.of(employeeOrderPosition));

        ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, 1L, "en", "test@gmail.com");

        verify(orderRepository, times(4)).findById(1L);
        verify(orderRepository, times(2)).save(order);
        verify(employeeRepository, times(1)).findByEmail("test@gmail.com");
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
        verify(paymentRepository).findAllByOrderId(1L);
        verify(receivingStationRepository).findAll();
        verify(employeeOrderPositionRepository).findAllByOrderId(1L);
        verify(employeeOrderPositionRepository).deleteAll(List.of(employeeOrderPosition));
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
        verifyNoMoreInteractions(orderRepository, employeeRepository, paymentRepository, receivingStationRepository,
            employeeOrderPositionRepository, tariffsInfoRepository);
    }

    @Test
    void updateOrderAdminPageInfoWithStatusCanceledTest() {
        Order order = getOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setOrderDate(LocalDateTime.now()).setTariffsInfo(tariffsInfo);
        order.setOrderStatus(OrderStatus.CANCELED);
        UpdateOrderPageAdminDto updateOrderPageAdminDto = ModelUtils.updateOrderPageAdminDtoWithStatusCanceled();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, 1L, "en", "test@gmail.com"));

        assertEquals(String.format(ORDER_CAN_NOT_BE_UPDATED, OrderStatus.CANCELED), exception.getMessage());

        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderAdminPageInfoWithStatusDoneTest() {
        Order order = getOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setOrderDate(LocalDateTime.now()).setTariffsInfo(tariffsInfo);
        order.setOrderStatus(OrderStatus.DONE);
        UpdateOrderPageAdminDto updateOrderPageAdminDto = ModelUtils.updateOrderPageAdminDtoWithStatusCanceled();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, 1L, "en", "test@gmail.com"));

        assertEquals(String.format(ORDER_CAN_NOT_BE_UPDATED, OrderStatus.DONE), exception.getMessage());

        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderAdminPageInfoWithStatusBroughtItHimselfTest() {
        Order order = getOrder();
        LocalDateTime orderDate = LocalDateTime.of(2023, 6, 10, 12, 10);
        LocalDateTime deliverFrom = LocalDateTime.of(2023, 6, 16, 15, 30);
        LocalDateTime deliverTo = LocalDateTime.of(2023, 6, 16, 19, 30);
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setOrderDate(orderDate);
        order.setDateOfExport(deliverFrom.toLocalDate());
        order.setDeliverFrom(deliverFrom);
        order.setDeliverTo(deliverTo);
        order.setTariffsInfo(tariffsInfo);
        order.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF);
        Employee employee = getEmployee();
        UpdateOrderPageAdminDto updateOrderPageAdminDto =
            ModelUtils.updateOrderPageAdminDtoWithStatusBroughtItHimself();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(getPayment()));

        ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, 1L, "en", "test@gmail.com");

        assertEquals(1L, order.getReceivingStation().getId());
        assertEquals(deliverFrom.toLocalDate(), order.getDateOfExport());
        assertEquals(deliverFrom, order.getDeliverFrom());
        assertEquals(deliverTo, order.getDeliverTo());
        verify(orderRepository, times(2)).findById(1L);
        verify(orderRepository).save(order);
        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
        verify(paymentRepository).findAllByOrderId(1L);
        verifyNoMoreInteractions(orderRepository, employeeRepository, paymentRepository);
    }

    @Test
    void updateOrderAdminPageInfoWithNullFieldsTest() {
        Order order = getOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setOrderDate(LocalDateTime.now()).setTariffsInfo(tariffsInfo);
        order.setOrderStatus(OrderStatus.ON_THE_ROUTE);
        Employee employee = getEmployee();
        UpdateOrderPageAdminDto updateOrderPageAdminDto =
            ModelUtils.updateOrderPageAdminDtoWithNullFields();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(getPayment()));
        when(receivingStationRepository.findAll()).thenReturn(List.of(getReceivingStation()));

        ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, 1L, "en", "test@gmail.com");

        verify(orderRepository, times(4)).findById(1L);
        verify(orderRepository, times(3)).save(order);
        verify(employeeRepository, times(1)).findByEmail("test@gmail.com");
        verify(tariffsInfoRepository).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
        verify(paymentRepository).findAllByOrderId(1L);
        verify(receivingStationRepository).findAll();
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
        verifyNoMoreInteractions(orderRepository, employeeRepository, paymentRepository, receivingStationRepository,
            tariffsInfoRepository);
    }

    @Test
    void updateOrderAdminPageInfoTestThrowsException() {
        UpdateOrderPageAdminDto updateOrderPageAdminDto = updateOrderPageAdminDto();
        TariffsInfo tariffsInfo = getTariffsInfo();
        Order order = getOrder();
        order.setOrderDate(LocalDateTime.now()).setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        assertThrows(BadRequestException.class,
            () -> ubsManagementService.updateOrderAdminPageInfo(
                updateOrderPageAdminDto, 1L, "en", "test@gmail.com"));
        verify(orderRepository, atLeastOnce()).findById(1L);
        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void updateOrderAdminPageInfoForOrderWithStatusBroughtItHimselfTest() {
        UpdateOrderPageAdminDto updateOrderPageAdminDto = updateOrderPageAdminDto();
        updateOrderPageAdminDto.setGeneralOrderInfo(OrderDetailStatusRequestDto
            .builder()
            .orderStatus(String.valueOf(OrderStatus.DONE))
            .build());

        LocalDateTime orderDate = LocalDateTime.now();
        TariffsInfo tariffsInfo = getTariffsInfo();
        Order expected = getOrder();
        expected.setOrderDate(orderDate).setTariffsInfo(tariffsInfo);
        expected.setOrderStatus(OrderStatus.DONE);

        Order order = getOrder();
        order.setOrderDate(orderDate).setTariffsInfo(tariffsInfo);
        order.setOrderStatus(OrderStatus.BROUGHT_IT_HIMSELF);

        Employee employee = getEmployee();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(getPayment()));

        ubsManagementService.updateOrderAdminPageInfo(updateOrderPageAdminDto, 1L, "en", "test@gmail.com");

        verify(orderRepository, times(2)).findById(1L);
        verify(employeeRepository).findByEmail("test@gmail.com");
        verify(orderRepository).save(order);
        assertEquals(expected, order);
    }

    @Test
    void saveReason() {
        Order order = ModelUtils.getOrdersDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(order));

        ubsManagementService.saveReason(1L, "uu", new MultipartFile[2]);

        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderSumDetailsForFormedOrder() {
        CounterOrderDetailsDto dto = ModelUtils.getcounterOrderDetailsDto();
        Order order = getFormedOrder();
        order.setOrderDate(LocalDateTime.now());
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));

        doNothing().when(notificationService).notifyPaidOrder(order);
        doNothing().when(notificationService).notifyHalfPaidPackage(order);
        doNothing().when(notificationService).notifyCourierItineraryFormed(order);
        when(ubsManagementService.getOrderSumDetails(1L)).thenReturn(dto);
        Assertions.assertNotNull(order);
    }

    @Test
    void getOrderSumDetailsForFormedOrderWithUbsCourierSumAndWriteOffStationSum() {
        CounterOrderDetailsDto dto = ModelUtils.getcounterOrderDetailsDto();
        Order order = getFormedOrder();
        order.setOrderDate(LocalDateTime.now());
        order.setUbsCourierSum(50_00L);
        order.setWriteOffStationSum(100_00L);
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));

        doNothing().when(notificationService).notifyPaidOrder(order);
        doNothing().when(notificationService).notifyHalfPaidPackage(order);
        doNothing().when(notificationService).notifyCourierItineraryFormed(order);
        when(ubsManagementService.getOrderSumDetails(1L)).thenReturn(dto);
        Assertions.assertNotNull(order);
    }

    @Test
    void getOrderSumDetailsForCanceledPaidOrder() {
        CounterOrderDetailsDto dto = ModelUtils.getcounterOrderDetailsDto();
        Order order = ModelUtils.getCanceledPaidOrder();
        order.setOrderDate(LocalDateTime.now());
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));

        doNothing().when(notificationService).notifyPaidOrder(order);
        doNothing().when(notificationService).notifyHalfPaidPackage(order);
        doNothing().when(notificationService).notifyCourierItineraryFormed(order);
        when(ubsManagementService.getOrderSumDetails(1L)).thenReturn(dto);
        Assertions.assertNotNull(order);
    }

    @Test
    void getOrderSumDetailsForCanceledPaidOrderWithBags() {
        CounterOrderDetailsDto dto = ModelUtils.getcounterOrderDetailsDto();
        Order order = ModelUtils.getCanceledPaidOrder();
        order.setOrderDate(LocalDateTime.now());
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBag3list());

        doNothing().when(notificationService).notifyPaidOrder(order);
        doNothing().when(notificationService).notifyHalfPaidPackage(order);
        doNothing().when(notificationService).notifyCourierItineraryFormed(order);
        when(ubsManagementService.getOrderSumDetails(1L)).thenReturn(dto);
        Assertions.assertNotNull(order);
    }

    @Test
    void getOrderSumDetailsForAdjustmentPaidOrder() {
        CounterOrderDetailsDto dto = ModelUtils.getcounterOrderDetailsDto();
        Order order = ModelUtils.getAdjustmentPaidOrder();
        order.setOrderDate(LocalDateTime.now());
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));

        doNothing().when(notificationService).notifyPaidOrder(order);
        doNothing().when(notificationService).notifyHalfPaidPackage(order);
        doNothing().when(notificationService).notifyCourierItineraryFormed(order);
        when(ubsManagementService.getOrderSumDetails(1L)).thenReturn(dto);
        Assertions.assertNotNull(order);
    }

    @Test
    void getOrderSumDetailsForFormedHalfPaidOrder() {
        CounterOrderDetailsDto dto = ModelUtils.getcounterOrderDetailsDto();
        Order order = ModelUtils.getFormedHalfPaidOrder();
        order.setOrderDate(LocalDateTime.now());
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBaglist());

        doNothing().when(notificationService).notifyPaidOrder(order);
        doNothing().when(notificationService).notifyHalfPaidPackage(order);
        doNothing().when(notificationService).notifyCourierItineraryFormed(order);
        when(ubsManagementService.getOrderSumDetails(1L)).thenReturn(dto);
        Assertions.assertNotNull(order);
    }

    @Test
    void getOrderSumDetailsForFormedHalfPaidOrderWithDiffBags() {
        CounterOrderDetailsDto dto = ModelUtils.getcounterOrderDetailsDto();
        Order order = ModelUtils.getFormedHalfPaidOrder();
        order.setOrderDate(LocalDateTime.now());
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBag3list());

        doNothing().when(notificationService).notifyPaidOrder(order);
        doNothing().when(notificationService).notifyHalfPaidPackage(order);
        doNothing().when(notificationService).notifyCourierItineraryFormed(order);
        when(ubsManagementService.getOrderSumDetails(1L)).thenReturn(dto);
        Assertions.assertNotNull(order);
    }

    @Test
    void getOrderSumDetailsForCanceledHalfPaidOrder() {
        CounterOrderDetailsDto dto = ModelUtils.getcounterOrderDetailsDto();
        Order order = ModelUtils.getCanceledHalfPaidOrder();
        order.setOrderDate(LocalDateTime.now());
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBaglist());

        doNothing().when(notificationService).notifyPaidOrder(order);
        doNothing().when(notificationService).notifyHalfPaidPackage(order);
        doNothing().when(notificationService).notifyCourierItineraryFormed(order);
        when(ubsManagementService.getOrderSumDetails(1L)).thenReturn(dto);
        Assertions.assertNotNull(order);
    }

    @Test
    void getOrderSumDetailsThrowsUnexcitingOrderExceptionTest() {
        Assertions.assertThrows(NotFoundException.class, () -> ubsManagementService.getOrderSumDetails(1L));
    }

    @Test
    void getOrderStatusDataTest() {
        Order order = getOrderForGetOrderStatusData2Test();
        BagInfoDto bagInfoDto = getBagInfoDto();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBaglist());
        when(bagRepository.findBagsByTariffsInfoId(1L)).thenReturn(getBaglist());
        when(certificateRepository.findCertificate(1L)).thenReturn(getCertificateList());
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(serviceRepository.findServiceByTariffsInfoId(1L)).thenReturn(Optional.of(getService()));
        when(modelMapper.map(getBaglist().get(0), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(orderStatusTranslationRepository.getOrderStatusTranslationById(6L))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(orderStatusTranslationRepository.findAllBy()).thenReturn(getOrderStatusTranslations());
        when(
            orderPaymentStatusTranslationRepository.getById(1L))
                .thenReturn(OrderPaymentStatusTranslation.builder().translationValue("name").build());
        when(orderPaymentStatusTranslationRepository.getAllBy()).thenReturn(getOrderStatusPaymentTranslations());
        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(getReceivingList());

        ubsManagementService.getOrderStatusData(1L, "test@gmail.com");

        verify(orderRepository).getOrderDetails(1L);
        verify(bagRepository).findBagsByOrderId(1L);
        verify(bagRepository).findBagsByTariffsInfoId(1L);
        verify(certificateRepository).findCertificate(1L);
        verify(orderRepository, times(5)).findById(1L);
        verify(serviceRepository).findServiceByTariffsInfoId(1L);
        verify(modelMapper).map(getBaglist().get(0), BagInfoDto.class);
        verify(orderStatusTranslationRepository).getOrderStatusTranslationById(6L);
        verify(orderPaymentStatusTranslationRepository).getById(1L);
        verify(receivingStationRepository).findAll();
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
        verify(orderStatusTranslationRepository).findAllBy();
        verify(orderPaymentStatusTranslationRepository).getAllBy();
    }

    @Test
    void saveNewManualPaymentWhenImageNotNull() {
        User user = getTestUser();
        user.setRecipientName("Петро");
        user.setRecipientSurname("Петренко");
        Order order = getFormedOrder();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        Payment payment = getManualPayment();
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementdate("02-08-2021").amount(500L).receiptLink("link").paymentId("1").build();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any()))
            .thenReturn(payment);
        doNothing().when(eventService).save(OrderHistory.ADD_PAYMENT_MANUALLY + 1, "Петро" + "  " + "Петренко", order);
        ubsManagementService.saveNewManualPayment(1L, paymentDetails, Mockito.mock(MultipartFile.class),
            "test@gmail.com");

        verify(eventService, times(1))
            .save("Замовлення Оплачено", "Система", order);
        verify(paymentRepository, times(1)).save(any());
        verify(orderRepository, times(1)).findById(1L);
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void getOrderStatusDataTestEmptyPriceDetails() {
        Order order = getOrderForGetOrderStatusEmptyPriceDetails();
        BagInfoDto bagInfoDto = getBagInfoDto();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(bagRepository.findBagsByTariffsInfoId(1L)).thenReturn(getBaglist());
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBag2list());
        when(serviceRepository.findServiceByTariffsInfoId(1L)).thenReturn(Optional.empty());
        when(modelMapper.map(getBaglist().get(0), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(orderStatusTranslationRepository.getOrderStatusTranslationById(6L))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(
            orderPaymentStatusTranslationRepository.getById(1L))
                .thenReturn(OrderPaymentStatusTranslation.builder().translationValue("name").build());
        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(getReceivingList());
        when(modelMapper.map(getOrderForGetOrderStatusData2Test().getPayment().get(0), PaymentInfoDto.class))
            .thenReturn(getInfoPayment());

        ubsManagementService.getOrderStatusData(1L, "test@gmail.com");

        verify(orderRepository).getOrderDetails(1L);
        verify(bagRepository).findBagsByOrderId(1L);
        verify(bagRepository).findBagsByTariffsInfoId(1L);
        verify(certificateRepository).findCertificate(1L);
        verify(orderRepository, times(5)).findById(1L);
        verify(serviceRepository).findServiceByTariffsInfoId(1L);
        verify(modelMapper).map(getBaglist().get(0), BagInfoDto.class);
        verify(orderStatusTranslationRepository).getOrderStatusTranslationById(6L);
        verify(orderPaymentStatusTranslationRepository).getById(1L);
        verify(receivingStationRepository).findAll();
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());

    }

    @Test
    void getOrderStatusDataWithEmptyCertificateTest() {
        Order order = getOrderForGetOrderStatusData2Test();
        BagInfoDto bagInfoDto = getBagInfoDto();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBaglist());
        when(bagRepository.findBagsByTariffsInfoId(1L)).thenReturn(getBaglist());
        when(serviceRepository.findServiceByTariffsInfoId(1L)).thenReturn(Optional.of(getService()));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(modelMapper.map(getBaglist().get(0), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(orderStatusTranslationRepository.getOrderStatusTranslationById(6L))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(
            orderPaymentStatusTranslationRepository.getById(1L))
                .thenReturn(OrderPaymentStatusTranslation.builder().translationValue("name").build());
        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(getReceivingList());

        ubsManagementService.getOrderStatusData(1L, "test@gmail.com");

        verify(orderRepository).getOrderDetails(1L);
        verify(bagRepository).findBagsByOrderId(1L);
        verify(bagRepository).findBagsByTariffsInfoId(1L);
        verify(orderRepository, times(5)).findById(1L);
        verify(serviceRepository).findServiceByTariffsInfoId(1L);
        verify(modelMapper).map(getBaglist().get(0), BagInfoDto.class);
        verify(orderStatusTranslationRepository).getOrderStatusTranslationById(6L);
        verify(orderPaymentStatusTranslationRepository).getById(1L);
        verify(receivingStationRepository).findAll();
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void getOrderStatusDataWhenOrderTranslationIsNull() {
        Order order = getOrderForGetOrderStatusData2Test();
        BagInfoDto bagInfoDto = getBagInfoDto();
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBaglist());
        when(bagRepository.findBagsByTariffsInfoId(1L)).thenReturn(getBaglist());
        when(serviceRepository.findServiceByTariffsInfoId(1L)).thenReturn(Optional.of(getService()));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(modelMapper.map(getBaglist().get(0), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(
            orderPaymentStatusTranslationRepository.getById(1L))
                .thenReturn(OrderPaymentStatusTranslation.builder().translationValue("name").build());
        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(getReceivingList());

        ubsManagementService.getOrderStatusData(1L, "test@gmail.com");

        verify(orderRepository).getOrderDetails(1L);
        verify(bagRepository).findBagsByOrderId(1L);
        verify(bagRepository).findBagsByTariffsInfoId(1L);
        verify(orderRepository, times(5)).findById(1L);
        verify(serviceRepository).findServiceByTariffsInfoId(1L);
        verify(modelMapper).map(getBaglist().get(0), BagInfoDto.class);
        verify(orderStatusTranslationRepository).getOrderStatusTranslationById(6L);
        verify(orderPaymentStatusTranslationRepository).getById(1L);
        verify(receivingStationRepository).findAll();
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void getOrderStatusDataExceptionTest() {
        Order order = getOrderForGetOrderStatusData2Test();
        TariffsInfo tariffsInfo = getTariffsInfo();
        BagInfoDto bagInfoDto = getBagInfoDto();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBaglist());
        when(certificateRepository.findCertificate(1L)).thenReturn(getCertificateList());
        when(serviceRepository.findServiceByTariffsInfoId(1L)).thenReturn(Optional.of(getService()));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(modelMapper.map(getBaglist().get(0), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(orderStatusTranslationRepository.getOrderStatusTranslationById(6L))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(
            orderPaymentStatusTranslationRepository.getById(1L))
                .thenReturn(OrderPaymentStatusTranslation.builder().translationValue("name").build());
        assertThrows(NotFoundException.class, () -> ubsManagementService.getOrderStatusData(1L, "test@gmail.com"));
    }

    @Test
    void updateOrderExportDetails() {
        User user = getTestUser();
        Order order = getOrderDoneByUser();

        List<ReceivingStation> receivingStations = List.of(getReceivingStation());
        ExportDetailsDtoUpdate testDetails = getExportDetailsRequestToday();
        var receivingStation = getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(receivingStations);

        ubsManagementService.updateOrderExportDetails(user.getId(), testDetails, "test@gmail.com");

        assertEquals(OrderStatus.ON_THE_ROUTE, order.getOrderStatus());

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void updateOrderExportDetailsEmptyDetailsTest() {
        User user = getTestUser();
        Order order = getOrder();
        order.setDeliverFrom(null);
        List<ReceivingStation> receivingStations = List.of(getReceivingStation());
        ExportDetailsDtoUpdate emptyDetails = ExportDetailsDtoUpdate.builder().receivingStationId(1L).build();
        var receivingStation = getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(receivingStations);

        ubsManagementService.updateOrderExportDetails(user.getId(), emptyDetails, user.getUuid());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void updateOrderExportDetailsUserNotFoundExceptionTest() {
        ExportDetailsDtoUpdate testDetails = getExportDetailsRequest();
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.updateOrderExportDetails(1L, testDetails, "test@gmail.com"));
    }

    @Test
    void updateOrderExportDetailsUnexistingOrderExceptionTest() {
        ExportDetailsDtoUpdate testDetails = getExportDetailsRequest();
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.updateOrderExportDetails(1L, testDetails, "abc"));
    }

    @Test
    void updateOrderExportDetailsReceivingStationNotFoundExceptionTest() {
        Order order = getOrder();
        ExportDetailsDtoUpdate testDetails = getExportDetailsRequest();
        var receivingStation = getReceivingStation();
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(Collections.emptyList());
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.updateOrderExportDetails(1L, testDetails, "abc"));
    }

    @Test
    void getPaymentInfo() {
        Order order = getOrder();
        order.setOrderStatus(OrderStatus.DONE);
        order.setOrderPaymentStatus(OrderPaymentStatus.PAID);
        PaymentInfoDto paymentInfo = getInfoPayment();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(modelMapper.map(any(), eq(PaymentInfoDto.class))).thenReturn(paymentInfo);

        assertEquals(ModelUtils.getPaymentTableInfoDto(), ubsManagementService.getPaymentInfo(order.getId(), 100.));
    }

    @Test
    void getPaymentInfoWithoutPayment() {
        Order order = getOrderWithoutPayment();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertEquals(ModelUtils.getPaymentTableInfoDto2(), ubsManagementService.getPaymentInfo(order.getId(), 100.));
    }

    @Test
    void getPaymentInfoExceptionTest() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.getPaymentInfo(1L, 100.));
    }

    @Test
    void updateManualPayment() {
        Employee employee = getEmployee();
        Order order = getOrderUserFirst();
        Payment payment = getPayment();
        ManualPaymentRequestDto requestDto = getManualPaymentRequestDto();
        requestDto.setImagePath("");
        payment.setImagePath("abc");
        MockMultipartFile file = new MockMultipartFile("manualPaymentDto",
            "", "application/json", "random Bytes".getBytes());

        when(employeeRepository.findByUuid(employee.getUuid())).thenReturn(Optional.of(employee));
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenReturn(payment);
        when(orderRepository.getOrderDetails(order.getId())).thenReturn(Optional.of(order));

        ubsManagementService.updateManualPayment(payment.getId(), requestDto, file, employee.getUuid());

        verify(paymentRepository).save(any(Payment.class));
        verify(eventService, times(2)).save(any(), any(), any());
    }

    @Test
    void updateManualPaymentUserNotFoundExceptionTest() {
        when(employeeRepository.findByUuid(anyString())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
            () -> ubsManagementService.updateManualPayment(1L, null, null, "abc"));
    }

    @Test
    void updateManualPaymentPaymentNotFoundExceptionTest() {
        when(employeeRepository.findByUuid(anyString())).thenReturn(Optional.of(getEmployee()));
        when(paymentRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.updateManualPayment(1L, null, null, "abc"));
    }

    @Test
    void updateAllOrderAdminPageInfoUnexistingOrderExceptionTest() {
        Order order = getOrder();
        UpdateAllOrderPageDto updateAllOrderPageDto = updateAllOrderPageDto();
        when(orderRepository.findById(4L)).thenReturn(Optional.ofNullable(order));
        assertThrows(NotFoundException.class,
            () -> ubsManagementService.updateAllOrderAdminPageInfo(updateAllOrderPageDto, "uuid", "ua"));
    }

    @Test
    void updateAllOrderAdminPageInfoUpdateAdminPageInfoExceptionTest() {
        UpdateAllOrderPageDto updateAllOrderPageDto = updateAllOrderPageDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(Order.builder().build()));
        assertThrows(BadRequestException.class,
            () -> ubsManagementService.updateAllOrderAdminPageInfo(updateAllOrderPageDto, "uuid", "ua"));
    }

    @Test
    void updateAllOrderAdminPageInfoStatusConfirmedTest() {
        Order order = getOrder();
        order.setOrderDate(LocalDateTime.now());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(getReceivingStation()));
        when(receivingStationRepository.findAll()).thenReturn(List.of(getReceivingStation()));

        UpdateAllOrderPageDto expectedObject = updateAllOrderPageDto();
        UpdateAllOrderPageDto actual = updateAllOrderPageDto();
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");

        expectedObject = updateAllOrderPageDto();
        actual = updateAllOrderPageDto();
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");

        expectedObject = updateAllOrderPageDto();
        actual = updateAllOrderPageDto();
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");

        expectedObject = updateAllOrderPageDto();
        actual = updateAllOrderPageDto();
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");

        expectedObject = updateAllOrderPageDto();
        actual = updateAllOrderPageDto();
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");

        expectedObject = updateAllOrderPageDto();
        actual = updateAllOrderPageDto();
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");

        expectedObject = updateAllOrderPageDto();
        actual = updateAllOrderPageDto();
        assertEquals(expectedObject.getExportDetailsDto().getDateExport(),
            actual.getExportDetailsDto().getDateExport());

        ubsManagementService.updateAllOrderAdminPageInfo(expectedObject, "uuid", "ua");
    }

    @Test
    void updateAllOrderAdminPageInfoAdditionalOrdersEmptyTest() {
        Order order = ModelUtils.getOrder2();
        UpdateAllOrderPageDto updateAllOrderPageDto = updateAllOrderPageDto();
        order.setOrderDate(LocalDateTime.now());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(getReceivingStation()));
        when(receivingStationRepository.findAll()).thenReturn(List.of(getReceivingStation()));

        ubsManagementService.updateAllOrderAdminPageInfo(updateAllOrderPageDto, "test@gmail.com", "ua");

        verify(orderRepository, times(2)).findById(1L);
    }

    @Test
    void checkGetPaymentInfoWhenPaymentsWithCertificatesAndPointsSmallerThanSumToPay() {
        Order order = getOrder();
        order.setOrderStatus(OrderStatus.DONE);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        assertEquals(0L, ubsManagementService.getPaymentInfo(order.getId(), 1100.).getOverpayment());
    }

    @Test
    void testAddPointsToUserWhenCurrentPointIsNull() {
        User user = getTestUser();
        user.setUuid(userRemoteClient.findUuidByEmail(user.getRecipientEmail()));
        user.setCurrentPoints(null);

        when(userRepository.findUserByUuid(user.getUuid())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        ubsManagementService.addPointsToUser(AddingPointsToUserDto.builder().additionalPoints(anyInt()).build());

        assertEquals(2L, user.getChangeOfPointsList().size());
    }

    @Test
    void saveReasonWhenListElementsAreNotNulls() {
        Order order = ModelUtils.getOrdersDto();
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(order));

        ubsManagementService.saveReason(1L, "uu", new MultipartFile[] {
            new MockMultipartFile("Name", new byte[2]), new MockMultipartFile("Name", new byte[2])});

        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderStatusDataWithNotEmptyLists() {
        Order order = getOrderForGetOrderStatusData2Test();
        BagInfoDto bagInfoDto = getBagInfoDto();
        OrderPaymentStatusTranslation orderPaymentStatusTranslation = mock(OrderPaymentStatusTranslation.class);
        TariffsInfo tariffsInfo = getTariffsInfo();
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findBagsByTariffsInfoId(1L)).thenReturn(getBaglist());
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBaglist());
        when(certificateRepository.findCertificate(1L)).thenReturn(getCertificateList());
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(modelMapper.map(getBaglist().get(0), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(orderStatusTranslationRepository.getOrderStatusTranslationById(6L))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(
            orderPaymentStatusTranslationRepository.getById(1L))
                .thenReturn(OrderPaymentStatusTranslation.builder().translationValue("name").build());
        when(
            orderPaymentStatusTranslationRepository.getAllBy())
                .thenReturn(List.of(orderPaymentStatusTranslation));

        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(getReceivingList());
        ubsManagementService.getOrderStatusData(1L, "test@gmail.com");

        verify(orderRepository).getOrderDetails(1L);
        verify(bagRepository).findBagsByTariffsInfoId(1L);
        verify(bagRepository).findBagsByOrderId(1L);
        verify(certificateRepository).findCertificate(1L);
        verify(orderRepository, times(5)).findById(1L);
        verify(modelMapper).map(getBaglist().get(0), BagInfoDto.class);
        verify(orderStatusTranslationRepository).getOrderStatusTranslationById(6L);
        verify(orderPaymentStatusTranslationRepository).getById(
            1L);
        verify(receivingStationRepository).findAll();
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());
    }

    @Test
    void getOrderStatusesTranslationTest() {
        Order order = getOrderForGetOrderStatusData2Test();
        BagInfoDto bagInfoDto = getBagInfoDto();
        OrderPaymentStatusTranslation orderPaymentStatusTranslation = mock(OrderPaymentStatusTranslation.class);
        List<OrderStatusTranslation> list = new ArrayList<>();
        TariffsInfo tariffsInfo = getTariffsInfo();
        list.add(getOrderStatusTranslation());
        order.setTariffsInfo(tariffsInfo);
        Employee employee = getEmployee();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findTariffsInfoByIdForEmployee(anyLong(), anyLong()))
            .thenReturn(Optional.of(tariffsInfo));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBaglist());
        when(bagRepository.findBagsByTariffsInfoId(1L)).thenReturn(getBaglist());
        when(certificateRepository.findCertificate(1L)).thenReturn(getCertificateList());
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(getOrderForGetOrderStatusData2Test()));
        when(modelMapper.map(getBaglist().get(0), BagInfoDto.class)).thenReturn(bagInfoDto);
        when(orderStatusTranslationRepository.getOrderStatusTranslationById(6L))
            .thenReturn(Optional.ofNullable(getStatusTranslation()));
        when(
            orderPaymentStatusTranslationRepository.getById(1L))
                .thenReturn(OrderPaymentStatusTranslation.builder().translationValue("name").build());

        when(orderStatusTranslationRepository.findAllBy())
            .thenReturn(list);

        when(
            orderPaymentStatusTranslationRepository.getAllBy())
                .thenReturn(List.of(orderPaymentStatusTranslation));

        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(getReceivingList());

        ubsManagementService.getOrderStatusData(1L, "test@gmail.com");

        verify(orderRepository).getOrderDetails(1L);
        verify(bagRepository).findBagsByOrderId(1L);
        verify(bagRepository).findBagsByTariffsInfoId(1L);
        verify(certificateRepository).findCertificate(1L);
        verify(orderRepository, times(5)).findById(1L);
        verify(modelMapper).map(getBaglist().get(0), BagInfoDto.class);
        verify(orderStatusTranslationRepository).getOrderStatusTranslationById(6L);
        verify(orderPaymentStatusTranslationRepository).getById(
            1L);
        verify(receivingStationRepository).findAll();
        verify(tariffsInfoRepository, atLeastOnce()).findTariffsInfoByIdForEmployee(anyLong(), anyLong());

    }

    @Test
    void deleteManualPaymentTest() {
        Payment payment = getManualPayment();
        Employee employee = getEmployee();
        Order order = getFormedOrder();

        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(employee));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        ubsManagementService.deleteManualPayment(1L, "abc");

        verify(employeeRepository).findByUuid("abc");
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).deletePaymentById(1L);
        verify(fileService).delete(payment.getImagePath());
        verify(eventService).save(OrderHistory.DELETE_PAYMENT_MANUALLY + payment.getPaymentId(),
            employee.getFirstName() + "  " + employee.getLastName(), payment.getOrder());

    }

    @Test
    void deleteManualTestWithoutImage() {
        Payment payment = getManualPayment();
        Employee employee = getEmployee();
        Order order = getFormedOrder();
        payment.setImagePath(null);

        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(employee));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        ubsManagementService.deleteManualPayment(1L, "abc");

        verify(employeeRepository).findByUuid("abc");
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).deletePaymentById(1L);
        verify(fileService, times(0)).delete(payment.getImagePath());
        verify(eventService).save(OrderHistory.DELETE_PAYMENT_MANUALLY + payment.getPaymentId(),
            employee.getFirstName() + "  " + employee.getLastName(), payment.getOrder());
    }

    @Test
    void deleteManualTestWithoutUser() {
        when(employeeRepository.findByUuid("uuid25")).thenReturn(Optional.empty());
        EntityNotFoundException ex =
            assertThrows(EntityNotFoundException.class, () -> ubsManagementService.deleteManualPayment(1L, "uuid25"));
        assertEquals(EMPLOYEE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void deleteManualTestWithoutPayment() {
        Employee employee = getEmployee();
        when(employeeRepository.findByUuid("abc")).thenReturn(Optional.of(employee));
        when(paymentRepository.findById(25L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> ubsManagementService.deleteManualPayment(25L, "abc"));
    }

    @Test
    void addBonusesToUserTest() {
        Order order = getOrderForGetOrderStatusData2Test();
        User user = order.getUser();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBaglist());
        when(certificateRepository.findCertificate(order.getId())).thenReturn(getCertificateList());

        ubsManagementService.addBonusesToUser(getAddBonusesToUserDto(), 1L, employee.getEmail());

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
        verify(userRepository).save(user);
        verify(notificationService).notifyBonuses(order, 809L);
        verify(eventService).saveEvent(OrderHistory.ADDED_BONUSES, employee.getEmail(), order);
    }

    @Test
    void addBonusesToUserIfOrderStatusIsCanceled() {
        Order order = getOrderForGetOrderStatusData2Test();
        order.setOrderStatus(OrderStatus.CANCELED);
        User user = order.getUser();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBaglist());
        when(certificateRepository.findCertificate(order.getId())).thenReturn(getCertificateList());

        ubsManagementService.addBonusesToUser(getAddBonusesToUserDto(), 1L, employee.getEmail());

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
        verify(userRepository).save(user);
        verify(notificationService).notifyBonuses(order, 200L);
        verify(eventService).saveEvent(OrderHistory.ADDED_BONUSES, employee.getEmail(), order);
    }

    @Test
    void addBonusesToUserWithoutExportedBagsTest() {
        Order order = ModelUtils.getOrderWithoutExportedBags();
        User user = order.getUser();
        Employee employee = getEmployee();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBaglist());
        when(certificateRepository.findCertificate(order.getId())).thenReturn(getCertificateList());

        ubsManagementService.addBonusesToUser(getAddBonusesToUserDto(), 1L, employee.getEmail());

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
        verify(userRepository).save(user);
        verify(notificationService).notifyBonuses(order, 209L);
        verify(eventService).saveEvent(OrderHistory.ADDED_BONUSES, employee.getEmail(), order);
    }

    @Test
    void addBonusesToUserWithoutOrderTest() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        AddBonusesToUserDto dto = getAddBonusesToUserDto();
        String email = getEmployee().getEmail();
        assertThrows(NotFoundException.class, () -> ubsManagementService.addBonusesToUser(dto, 1L, email));
    }

    @Test
    void addBonusesToUserWithNoOverpaymentTest() {
        Order order = getOrderForGetOrderStatusData2Test();
        String email = getEmployee().getEmail();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.getOrderDetails(1L)).thenReturn(Optional.of(order));
        when(bagRepository.findBagsByOrderId(1L)).thenReturn(getBag3list());
        when(certificateRepository.findCertificate(order.getId())).thenReturn(getCertificateList());

        AddBonusesToUserDto addBonusesToUserDto = getAddBonusesToUserDto();
        assertThrows(BadRequestException.class,
            () -> ubsManagementService.addBonusesToUser(addBonusesToUserDto, 1L, email));
    }

    @Test
    void checkEmployeeForOrderTest() {
        User user = getTestUser();
        Order order = user.getOrders().get(0);
        order.setOrderStatus(OrderStatus.CANCELED).setTariffsInfo(getTariffsInfo());
        Employee employee = getEmployee();
        List<Long> tariffsInfoIds = new ArrayList<>();
        tariffsInfoIds.add(1L);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(employee));
        when(employeeRepository.findTariffsInfoForEmployee(employee.getId())).thenReturn(tariffsInfoIds);
        assertEquals(true, ubsManagementService.checkEmployeeForOrder(order.getId(), "test@gmail.com"));
    }

    @Test
    void updateOrderStatusToExpected() {
        ubsManagementService.updateOrderStatusToExpected();
        verify(orderRepository).updateOrderStatusToExpected(OrderStatus.CONFIRMED.name(),
            OrderStatus.ON_THE_ROUTE.name(),
            LocalDate.now());
    }

    @ParameterizedTest
    @MethodSource("provideOrdersWithDifferentInitialExportDetailsForUpdateOrderExportDetails")
    void updateOrderExportDetailsSettingForDifferentInitialExportDetailsTest(Order order, String expectedHistoryEvent) {
        Employee employee = getEmployee();
        List<ReceivingStation> receivingStations = List.of(getReceivingStation());
        ExportDetailsDtoUpdate testDetails = getExportDetailsRequest();
        var receivingStation = getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(receivingStations);

        ubsManagementService.updateOrderExportDetails(order.getId(), testDetails, employee.getEmail());
        verify(orderRepository, times(1)).save(order);
        verify(eventService, times(1)).saveEvent(expectedHistoryEvent, employee.getEmail(), order);
    }

    private static Stream<Arguments> provideOrdersWithDifferentInitialExportDetailsForUpdateOrderExportDetails() {
        Order orderWithoutExportDate = getOrderExportDetails();
        orderWithoutExportDate.setDateOfExport(null);
        Order orderWithoutDeliverFromTo = getOrderExportDetails();
        orderWithoutDeliverFromTo.setDeliverFrom(null);
        orderWithoutDeliverFromTo.setDeliverTo(null);
        String updateExportDetails = String.format(OrderHistory.UPDATE_EXPORT_DATA,
            LocalDate.of(1997, 12, 4)) +
            String.format(OrderHistory.UPDATE_DELIVERY_TIME,
                LocalTime.of(15, 40, 24), LocalTime.of(19, 30, 30))
            +
            String.format(OrderHistory.UPDATE_RECEIVING_STATION, "Петрівка");
        return Stream.of(
            Arguments.of(getOrderExportDetailsWithNullValues(),
                OrderHistory.SET_EXPORT_DETAILS + updateExportDetails),
            Arguments.of(getOrderExportDetailsWithExportDate(),
                OrderHistory.UPDATE_EXPORT_DETAILS + updateExportDetails),
            Arguments.of(getOrderExportDetailsWithExportDateDeliverFrom(),
                OrderHistory.UPDATE_EXPORT_DETAILS + updateExportDetails),
            Arguments.of(getOrderExportDetailsWithExportDateDeliverFromTo(),
                OrderHistory.UPDATE_EXPORT_DETAILS + updateExportDetails),
            Arguments.of(getOrderExportDetails(),
                OrderHistory.UPDATE_EXPORT_DETAILS + updateExportDetails),
            Arguments.of(getOrderExportDetailsWithDeliverFromTo(),
                OrderHistory.UPDATE_EXPORT_DETAILS + updateExportDetails),
            Arguments.of(orderWithoutExportDate,
                OrderHistory.UPDATE_EXPORT_DETAILS + updateExportDetails),
            Arguments.of(orderWithoutDeliverFromTo,
                OrderHistory.UPDATE_EXPORT_DETAILS + updateExportDetails));
    }

    @Test
    void updateOrderExportDetailsWhenDeliverFromIsNull() {
        Employee employee = getEmployee();
        List<ReceivingStation> receivingStations = List.of(getReceivingStation());
        ExportDetailsDtoUpdate testDetails = getExportDetailsRequest();
        Order order = getOrderExportDetails();
        var receivingStation = getReceivingStation();
        order.setDeliverFrom(null);
        testDetails.setTimeDeliveryFrom(null);
        String expectedHistoryEvent = OrderHistory.UPDATE_EXPORT_DETAILS
            + String.format(OrderHistory.UPDATE_EXPORT_DATA,
                LocalDate.of(1997, 12, 4))
            +
            String.format(OrderHistory.UPDATE_RECEIVING_STATION, "Петрівка");

        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(receivingStations);

        ubsManagementService.updateOrderExportDetails(order.getId(), testDetails, employee.getEmail());
        verify(orderRepository, times(1)).save(order);
        verify(eventService, times(1)).saveEvent(expectedHistoryEvent, employee.getEmail(), order);
    }

    @Test
    void updateOrderExportDetailsWhenDeliverToIsNull() {
        Employee employee = getEmployee();
        List<ReceivingStation> receivingStations = List.of(getReceivingStation());
        ExportDetailsDtoUpdate testDetails = getExportDetailsRequest();
        Order order = getOrderExportDetails();
        var receivingStation = getReceivingStation();
        order.setDeliverTo(null);
        testDetails.setTimeDeliveryTo(null);
        String expectedHistoryEvent = OrderHistory.UPDATE_EXPORT_DETAILS
            + String.format(OrderHistory.UPDATE_EXPORT_DATA,
                LocalDate.of(1997, 12, 4))
            +
            String.format(OrderHistory.UPDATE_RECEIVING_STATION, "Петрівка");

        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(receivingStationRepository.findAll()).thenReturn(receivingStations);

        ubsManagementService.updateOrderExportDetails(order.getId(), testDetails, employee.getEmail());
        verify(orderRepository, times(1)).save(order);
        verify(eventService, times(1)).saveEvent(expectedHistoryEvent, employee.getEmail(), order);
    }

    @Test
    void getOrderCancellationReasonTest() {
        OrderCancellationReasonDto cancellationReasonDto = ModelUtils.getCancellationDto();
        Order order = ModelUtils.getOrderTest();
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(order));

        OrderCancellationReasonDto result = ubsManagementService.getOrderCancellationReason(1L);
        assertEquals(cancellationReasonDto.getCancellationReason(), result.getCancellationReason());
        assertEquals(cancellationReasonDto.getCancellationComment(), result.getCancellationComment());
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderCancellationReasonWithoutOrderTest() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> ubsManagementService.getOrderCancellationReason(1L));
    }

    @Test
    void getNotTakenOrderReasonTest() {
        NotTakenOrderReasonDto notTakenOrderReasonDto = ModelUtils.getNotTakenOrderReasonDto();
        Order order = ModelUtils.getTestNotTakenOrderReason();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        NotTakenOrderReasonDto result = ubsManagementService.getNotTakenOrderReason(1L);

        assertEquals(notTakenOrderReasonDto.getDescription(), result.getDescription());
        assertEquals(notTakenOrderReasonDto.getImages(), result.getImages());
        verify(orderRepository).findById(1L);
    }

    @Test
    void getNotTakenOrderReasonWithoutOrderTest() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> ubsManagementService.getNotTakenOrderReason(1L));
        verify(orderRepository).findById(1L);
    }

    @Test
    void saveNewManualPaymentWithoutLinkAndImageTest() {
        ManualPaymentRequestDto paymentDetails = ManualPaymentRequestDto.builder()
            .settlementdate("02-08-2021").amount(500L).paymentId("1").build();
        assertThrows(BadRequestException.class,
            () -> ubsManagementService.saveNewManualPayment(1L, paymentDetails, null, "test@gmail.com"));
    }

    @Test
    void saveOrderIdForRefundTest() {
        Order order = getOrder();
        Refund refund = getRefund(order.getId());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(refundRepository.save(refund)).thenReturn(refund);
        ubsManagementService.saveOrderIdForRefund(1L);
        verify(orderRepository).findById(1L);
        verify(refundRepository).save(refund);
    }

    @Test
    void saveOrderIdForRefundThrowsNotFoundExceptionTest() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> ubsManagementService.getNotTakenOrderReason(1L));
        verify(orderRepository).findById(1L);
    }
}