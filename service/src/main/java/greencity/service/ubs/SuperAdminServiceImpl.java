package greencity.service.ubs;

import greencity.constant.ErrorMessage;
import greencity.dto.AddNewTariffDto;
import greencity.dto.DetailsOfDeactivateTariffsDto;
import greencity.dto.LocationsDtos;
import greencity.dto.bag.EditAmountOfBagDto;
import greencity.dto.courier.*;
import greencity.dto.location.AddLocationTranslationDto;
import greencity.dto.location.EditLocationDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.location.LocationInfoDto;
import greencity.dto.order.EditPriceOfOrder;
import greencity.dto.service.AddServiceDto;
import greencity.dto.service.CreateServiceDto;
import greencity.dto.service.EditServiceDto;
import greencity.dto.service.GetServiceDto;
import greencity.dto.tariff.*;
import greencity.entity.coords.Coordinates;
import greencity.entity.order.*;
import greencity.entity.user.Location;
import greencity.entity.user.Region;
import greencity.entity.user.User;
import greencity.entity.user.employee.ReceivingStation;
import greencity.enums.CourierLimit;
import greencity.enums.CourierStatus;
import greencity.enums.LocationStatus;
import greencity.enums.MinAmountOfBag;
import greencity.enums.StationStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.UnprocessableEntityException;
import greencity.exceptions.courier.CourierAlreadyExists;
import greencity.exceptions.tariff.TariffAlreadyExistsException;
import greencity.filters.TariffsInfoFilterCriteria;
import greencity.filters.TariffsInfoSpecification;
import greencity.repository.*;
import greencity.service.SuperAdminService;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Data
public class SuperAdminServiceImpl implements SuperAdminService {
    private final BagRepository bagRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceTranslationRepository serviceTranslationRepository;
    private final LocationRepository locationRepository;
    private final CourierRepository courierRepository;
    private final RegionRepository regionRepository;
    private final ReceivingStationRepository receivingStationRepository;
    private final TariffsInfoRepository tariffsInfoRepository;
    private final ModelMapper modelMapper;
    private final TariffLocationRepository tariffsLocationRepository;

    private final DeactivateChosenEntityRepository deactivateTariffsForChosenParamRepository;
    private static final String BAD_SIZE_OF_REGIONS_MESSAGE =
        "Region ids size should be 1 if several params are selected";
    private static final String REGIONS_EXIST_MESSAGE = "Current region doesn't exist: %s";
    private static final String REGIONS_OR_CITIES_EXIST_MESSAGE = "Current regions %s or cities %s don't exist.";
    private static final String COURIER_EXISTS_MESSAGE = "Current courier doesn't exist: %s";
    private static final String RECEIVING_STATIONS_EXIST_MESSAGE = "Current receiving stations don't exist: %s";
    private static final String RECEIVING_STATIONS_OR_COURIER_EXIST_MESSAGE =
        "Current receiving stations: %s or courier: %s don't exist.";
    private static final String REGION_OR_COURIER_EXIST_MESSAGE = "Current region: %s or courier: %s don't exist.";
    private static final String REGION_OR_CITIES_OR_RECEIVING_STATIONS_EXIST_MESSAGE =
        "Current region: %s or cities: %s or receiving stations: %s don't exist.";
    private static final String REGION_OR_CITIES_OR_RECEIVING_STATIONS_OR_COURIER_EXIST_MESSAGE =
        "Current region: %s or cities: %s or receiving stations: %s or courier: %s don't exist.";

    @Override
    public AddServiceDto addTariffService(AddServiceDto dto, String uuid) {
        User user = userRepository.findByUuid(uuid);
        Bag bag = createBagWithFewTranslation(dto, user);
        bagRepository.save(bag);
        return modelMapper.map(bag, AddServiceDto.class);
    }

    private Bag createBagWithFewTranslation(AddServiceDto dto, User user) {
        final Location location = locationRepository.findById(dto.getLocationId()).orElseThrow(
            () -> new NotFoundException(ErrorMessage.LOCATION_DOESNT_FOUND));
        return Bag.builder().price(dto.getPrice())
            .capacity(dto.getCapacity())
            .location(location)
            .commission(dto.getCommission())
            .fullPrice(getFullPrice(dto.getPrice(), dto.getCommission()))
            .createdBy(user.getRecipientName() + " " + user.getRecipientSurname())
            .createdAt(LocalDate.now())
            .minAmountOfBags(MinAmountOfBag.INCLUDE)
            .name(dto.getTariffTranslationDto().getName())
            .nameEng(dto.getTariffTranslationDto().getNameEng())
            .description(dto.getTariffTranslationDto().getDescription())
            .descriptionEng(dto.getTariffTranslationDto().getDescriptionEng())
            .build();
    }

    @Override
    public List<GetTariffServiceDto> getTariffService() {
        return bagRepository.findAll()
            .stream()
            .map(this::getTariffService)
            .collect(Collectors.toList());
    }

    private GetTariffServiceDto getTariffService(Bag bag) {
        return GetTariffServiceDto.builder()
            .description(bag.getDescription())
            .descriptionEng(bag.getDescriptionEng())
            .price(bag.getPrice())
            .capacity(bag.getCapacity())
            .name(bag.getName())
            .commission(bag.getCommission())
            .nameEng(bag.getNameEng())
            .fullPrice(bag.getFullPrice())
            .id(bag.getId())
            .createdAt(bag.getCreatedAt())
            .createdBy(bag.getCreatedBy())
            .editedAt(bag.getEditedAt())
            .editedBy(bag.getEditedBy())
            .locationId(bag.getLocation().getId())
            .minAmountOfBag(bag.getMinAmountOfBags().toString())
            .build();
    }

    @Override
    public void deleteTariffService(Integer id) {
        Bag bag = bagRepository.findById(id).orElseThrow(
            () -> new NotFoundException(ErrorMessage.BAG_NOT_FOUND + id));
        bagRepository.delete(bag);
    }

    @Override
    public GetTariffServiceDto editTariffService(EditTariffServiceDto dto, Integer id, String uuid) {
        User user = userRepository.findByUuid(uuid);
        Bag bag = bagRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorMessage.BAG_NOT_FOUND));
        bag.setPrice(dto.getPrice());
        bag.setCapacity(dto.getCapacity());
        bag.setCommission(dto.getCommission());
        bag.setFullPrice(getFullPrice(dto.getPrice(), dto.getCommission()));
        bag.setEditedAt(LocalDate.now());
        bag.setEditedBy(user.getRecipientName() + " " + user.getRecipientSurname());
        bag.setName(dto.getName());
        bag.setDescription(dto.getDescription());
        bagRepository.save(bag);
        return getTariffService(bag);
    }

    @Override
    public CreateServiceDto addService(CreateServiceDto dto, String uuid) {
        User user = userRepository.findByUuid(uuid);
        Service service = createServiceWithTranslation(dto, user);
        service.setFullPrice(getFullPrice(dto.getPrice(), dto.getCommission()));
        serviceRepository.save(service);
        serviceTranslationRepository.saveAll(service.getServiceTranslations());
        return modelMapper.map(service, CreateServiceDto.class);
    }

    private Service createServiceWithTranslation(CreateServiceDto dto, User user) {
        Long id = dto.getCourierId();
        Courier courier = courierRepository.findById(id).orElseThrow(
            () -> new NotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID + id));

        Service service = Service.builder()
            .basePrice(dto.getPrice())
            .commission(dto.getCommission())
            .fullPrice(dto.getPrice() + dto.getCommission())
            .capacity(dto.getCapacity())
            .createdAt(LocalDate.now())
            .courier(courier)
            .createdBy(user.getRecipientName() + " " + user.getRecipientSurname())
            .serviceTranslations(dto.getServiceTranslationDtoList()
                .stream().map(serviceTranslationDto -> ServiceTranslation.builder()
                    .description(serviceTranslationDto.getDescription())
                    .descriptionEng(serviceTranslationDto.getDescriptionEng())
                    .name(serviceTranslationDto.getName())
                    .nameEng(serviceTranslationDto.getNameEng())
                    .build())
                .collect(Collectors.toList()))
            .build();
        service.getServiceTranslations().forEach(serviceTranslation -> serviceTranslation.setService(service));
        return service;
    }

    @Override
    public List<GetServiceDto> getService() {
        return serviceTranslationRepository.findAll()
            .stream()
            .map(this::getService)
            .collect(Collectors.toList());
    }

    private GetServiceDto getService(ServiceTranslation serviceTranslation) {
        return GetServiceDto.builder()
            .courierId(serviceTranslation.getService().getCourier().getId())
            .description(serviceTranslation.getDescription())
            .descriptionEng(serviceTranslation.getDescriptionEng())
            .price(serviceTranslation.getService().getBasePrice())
            .capacity(serviceTranslation.getService().getCapacity())
            .name(serviceTranslation.getName())
            .nameEng(serviceTranslation.getNameEng())
            .commission(serviceTranslation.getService().getCommission())
            .fullPrice(serviceTranslation.getService().getFullPrice())
            .id(serviceTranslation.getService().getId())
            .createdAt(serviceTranslation.getService().getCreatedAt())
            .createdBy(serviceTranslation.getService().getCreatedBy())
            .editedAt(serviceTranslation.getService().getEditedAt())
            .editedBy(serviceTranslation.getService().getEditedBy())
            .build();
    }

    @Override
    public void deleteService(long id) {
        Service service = serviceRepository.findById(id).orElseThrow(
            () -> new NotFoundException(ErrorMessage.SERVICE_IS_NOT_FOUND_BY_ID + id));
        serviceRepository.delete(service);
    }

    @Override
    public GetServiceDto editService(long id, EditServiceDto dto, String uuid) {
        Service service = serviceRepository.findServiceById(id).orElseThrow(
            () -> new NotFoundException(ErrorMessage.SERVICE_IS_NOT_FOUND_BY_ID + id));
        User user = userRepository.findByUuid(uuid);
        service.setCapacity(dto.getCapacity());
        service.setCommission(dto.getCommission());
        service.setBasePrice(dto.getPrice());
        service.setEditedAt(LocalDate.now());
        service.setEditedBy(user.getRecipientName() + " " + user.getRecipientSurname());
        ServiceTranslation serviceTranslation = serviceTranslationRepository
            .findServiceTranslationsByService(service);
        serviceTranslation.setService(service);
        serviceTranslation.setName(dto.getName());
        serviceTranslation.setNameEng(dto.getNameEng());
        serviceTranslation.setDescription(dto.getDescription());
        serviceTranslation.setDescriptionEng(dto.getDescriptionEng());
        service.setFullPrice(dto.getPrice() + dto.getCommission());
        service.setBasePrice(dto.getPrice());
        serviceRepository.save(service);
        return getService(serviceTranslation);
    }

    @Override
    public List<LocationInfoDto> getAllLocation() {
        return regionRepository.findAll().stream()
            .map(i -> modelMapper.map(i, LocationInfoDto.class))
            .collect(Collectors.toList());
    }

    @Override
    public List<LocationInfoDto> getActiveLocations() {
        return regionRepository.findRegionsWithActiveLocations().stream()
            .distinct()
            .map(region -> modelMapper.map(region, LocationInfoDto.class))
            .collect(Collectors.toList());
    }

    @Override
    public void addLocation(List<LocationCreateDto> dtoList) {
        dtoList.forEach(locationCreateDto -> {
            Region region = checkIfRegionAlreadyCreated(locationCreateDto);
            Location location = createNewLocation(locationCreateDto, region);
            checkIfLocationAlreadyCreated(locationCreateDto.getAddLocationDtoList(), region.getId());
            locationRepository.save(location);
        });
    }

    private Location createNewLocation(LocationCreateDto dto, Region region) {
        return Location.builder()
            .locationStatus(LocationStatus.ACTIVE)
            .coordinates(Coordinates.builder().latitude(dto.getLatitude()).longitude(dto.getLongitude()).build())
            .nameEn(dto.getAddLocationDtoList().stream().filter(x -> x.getLanguageCode().equals("en")).findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_ERROR))
                .getLocationName())
            .nameUk(dto.getAddLocationDtoList().stream().filter(x -> x.getLanguageCode().equals("ua")).findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_ERROR))
                .getLocationName())
            .region(region)
            .build();
    }

    private void checkIfLocationAlreadyCreated(List<AddLocationTranslationDto> dto, Long regionId) {
        Optional<Location> location = locationRepository.findLocationByNameAndRegionId(
            dto.stream().filter(translation -> translation.getLanguageCode().equals("ua")).findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_ERROR))
                .getLocationName(),
            dto.stream().filter(translation -> translation.getLanguageCode().equals("en")).findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_ERROR))
                .getLocationName(),
            regionId);

        if (location.isPresent()) {
            throw new NotFoundException("The location with name: "
                + dto.get(0).getLocationName() + ErrorMessage.LOCATION_ALREADY_EXIST);
        }
    }

    private Region checkIfRegionAlreadyCreated(LocationCreateDto dto) {
        String enName = dto.getRegionTranslationDtos().stream()
            .filter(regionTranslationDto -> regionTranslationDto.getLanguageCode().equals("en")).findAny()
            .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_ERROR))
            .getRegionName();
        String ukName = dto.getRegionTranslationDtos().stream()
            .filter(regionTranslationDto -> regionTranslationDto.getLanguageCode().equals("ua")).findAny()
            .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_ERROR))
            .getRegionName();

        Region region = regionRepository.findRegionByEnNameAndUkrName(enName, ukName).orElse(null);

        if (null == region) {
            region = createRegionWithTranslation(dto);
            region = regionRepository.save(region);
        }
        return region;
    }

    @Override
    public void deactivateLocation(Long id) {
        Location location = tryToFindLocationById(id);
        if (LocationStatus.DEACTIVATED.equals(location.getLocationStatus())) {
            throw new BadRequestException(ErrorMessage.LOCATION_STATUS_IS_ALREADY_EXIST);
        }
        location.setLocationStatus(LocationStatus.DEACTIVATED);
        locationRepository.save(location);
    }

    @Override
    public void activateLocation(Long id) {
        Location location = tryToFindLocationById(id);
        if (LocationStatus.ACTIVE.equals(location.getLocationStatus())) {
            throw new BadRequestException(ErrorMessage.LOCATION_STATUS_IS_ALREADY_EXIST);
        }
        location.setLocationStatus(LocationStatus.ACTIVE);
        locationRepository.save(location);
    }

    @Override
    public CreateCourierDto createCourier(CreateCourierDto dto, String uuid) {
        User user = userRepository.findByUuid(uuid);

        checkIfCourierAlreadyExists(courierRepository.findAll(), dto);

        Courier courier = new Courier();
        courier.setCreatedBy(user);
        courier.setCourierStatus(CourierStatus.ACTIVE);
        courier.setCreateDate(LocalDate.now());
        courier.setNameEn(dto.getNameEn());
        courier.setNameUk(dto.getNameUk());
        courierRepository.save(courier);
        return modelMapper.map(courier, CreateCourierDto.class);
    }

    private void checkIfCourierAlreadyExists(List<Courier> couriers, CreateCourierDto createCourierDto) {
        couriers
            .forEach(courier -> {
                if (courier.getNameEn().equals(createCourierDto.getNameEn())
                    || courier.getNameUk().equals(createCourierDto.getNameUk())) {
                    throw new CourierAlreadyExists(ErrorMessage.COURIER_ALREADY_EXISTS);
                }
            });
    }

    @Override
    public CourierDto updateCourier(CourierUpdateDto dto) {
        Courier courier = courierRepository.findById(dto.getCourierId())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID));
        courier.setNameUk(dto.getNameUk());
        courier.setNameEn(dto.getNameEn());
        courierRepository.save(courier);
        return modelMapper.map(courier, CourierDto.class);
    }

    @Override
    public List<CourierDto> getAllCouriers() {
        return courierRepository.findAll().stream().map(courier -> modelMapper.map(courier, CourierDto.class))
            .collect(Collectors.toList());
    }

    @Override
    public GetTariffsInfoDto setLimitDescription(Long tariffId, String limitDescription) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public GetTariffServiceDto includeBag(Integer id) {
        Bag bag = bagRepository.findById(id).orElseThrow(
            () -> new NotFoundException(ErrorMessage.BAG_NOT_FOUND + id));
        if (bag.getMinAmountOfBags().equals(MinAmountOfBag.INCLUDE)) {
            throw new BadRequestException(ErrorMessage.BAG_WITH_THIS_STATUS_ALREADY_SET);
        }
        bag.setMinAmountOfBags(MinAmountOfBag.INCLUDE);
        bagRepository.save(bag);
        return getTariffService(bag);
    }

    @Override
    public GetTariffServiceDto excludeBag(Integer id) {
        Bag bag = bagRepository.findById(id).orElseThrow(
            () -> new NotFoundException(ErrorMessage.BAG_NOT_FOUND + id));
        if (MinAmountOfBag.EXCLUDE.equals(bag.getMinAmountOfBags())) {
            throw new BadRequestException(ErrorMessage.BAG_WITH_THIS_STATUS_ALREADY_SET);
        }
        bag.setMinAmountOfBags(MinAmountOfBag.EXCLUDE);
        bagRepository.save(bag);
        return getTariffService(bag);
    }

    @Override
    public void deleteCourier(Long id) {
        Courier courier = courierRepository.findById(id).orElseThrow(
            () -> new NotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID + id));
        courier.setCourierStatus(CourierStatus.DELETED);
        courierRepository.save(courier);
    }

    private Integer getFullPrice(Integer price, Integer commission) {
        return price + commission;
    }

    @Override
    public List<GetTariffsInfoDto> getAllTariffsInfo(TariffsInfoFilterCriteria filterCriteria) {
        List<TariffsInfo> tariffs = tariffsInfoRepository.findAll(new TariffsInfoSpecification(filterCriteria));
        List<GetTariffsInfoDto> dtos = tariffs
            .stream()
            .map(tariffsInfo -> modelMapper.map(tariffsInfo, GetTariffsInfoDto.class))
            .sorted(Comparator.comparing(tariff -> tariff.getRegionDto().getNameUk()))
            .sorted(Comparator.comparing(tariff -> tariff.getTariffStatus().getPriority()))
            .collect(Collectors.toList());
        dtos.forEach(tariff -> tariff.setLocationInfoDtos(tariff.getLocationInfoDtos().stream()
            .sorted(Comparator.comparing(LocationsDtos::getNameUk))
            .collect(Collectors.toList())));
        return dtos;
    }

    private Region createRegionWithTranslation(LocationCreateDto dto) {
        String enName = dto.getRegionTranslationDtos().stream().filter(x -> x.getLanguageCode().equals("en")).findAny()
            .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_ERROR))
            .getRegionName();
        String uaName = dto.getRegionTranslationDtos().stream().filter(x -> x.getLanguageCode().equals("ua")).findAny()
            .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_ERROR))
            .getRegionName();

        return Region.builder()
            .enName(enName)
            .ukrName(uaName)
            .build();
    }

    private Location tryToFindLocationById(Long id) {
        return locationRepository.findById(id).orElseThrow(
            () -> new NotFoundException(ErrorMessage.LOCATION_DOESNT_FOUND));
    }

    @Override
    public ReceivingStationDto createReceivingStation(AddingReceivingStationDto dto, String uuid) {
        if (!receivingStationRepository.existsReceivingStationByName(dto.getName())) {
            User user = userRepository.findByUuid(uuid);
            ReceivingStation receivingStation = receivingStationRepository.save(buildReceivingStation(dto, user));
            return modelMapper.map(receivingStation, ReceivingStationDto.class);
        }
        throw new UnprocessableEntityException(
            ErrorMessage.RECEIVING_STATION_ALREADY_EXISTS + dto.getName());
    }

    private ReceivingStation buildReceivingStation(AddingReceivingStationDto dto, User user) {
        return ReceivingStation.builder()
            .name(dto.getName())
            .createdBy(user)
            .createDate(LocalDate.now())
            .stationStatus(StationStatus.ACTIVE)
            .build();
    }

    @Override
    public List<ReceivingStationDto> getAllReceivingStations() {
        return receivingStationRepository.findAll().stream()
            .map(r -> modelMapper.map(r, ReceivingStationDto.class))
            .collect(Collectors.toList());
    }

    @Override
    public ReceivingStationDto updateReceivingStation(ReceivingStationDto dto) {
        ReceivingStation receivingStation = receivingStationRepository.findById(dto.getId())
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.RECEIVING_STATION_NOT_FOUND_BY_ID + dto.getId()));
        receivingStation.setName(dto.getName());
        receivingStationRepository.save(receivingStation);
        return modelMapper.map(receivingStation, ReceivingStationDto.class);
    }

    @Override
    public void deleteReceivingStation(Long id) {
        ReceivingStation station = receivingStationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(
                ErrorMessage.RECEIVING_STATION_NOT_FOUND_BY_ID + id));
        receivingStationRepository.delete(station);
    }

    private Set<Location> findLocationsForTariff(List<Long> locationId, Long regionId) {
        Set<Location> locationSet = new HashSet<>(locationRepository
            .findAllByIdAndRegionId(locationId.stream().distinct().collect(Collectors.toList()), regionId));
        if (locationSet.isEmpty()) {
            throw new NotFoundException("List of locations can not be empty");
        }
        return locationSet;
    }

    private Set<ReceivingStation> findReceivingStationsForTariff(List<Long> receivingStationIdList) {
        Set<ReceivingStation> receivingStations = new HashSet<>(receivingStationRepository
            .findAllById(receivingStationIdList.stream().distinct().collect(Collectors.toList())));
        if (receivingStations.isEmpty()) {
            throw new NotFoundException("List of receiving stations can not be empty");
        }
        return receivingStations;
    }

    private TariffsInfo tryToFindTariffById(Long tariffId) {
        return tariffsInfoRepository.findById(tariffId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.TARIFF_NOT_FOUND + tariffId));
    }

    @Override
    @Transactional
    public AddNewTariffResponseDto addNewTariff(AddNewTariffDto addNewTariffDto, String userUUID) {
        Courier courier = tryToFindCourier(addNewTariffDto.getCourierId());
        List<Long> idListToCheck = new ArrayList<>(addNewTariffDto.getLocationIdList());
        final var tariffForLocationAndCourierAlreadyExistIdList =
            verifyIfTariffExists(idListToCheck, addNewTariffDto.getCourierId());
        TariffsInfo tariffsInfo = createTariff(addNewTariffDto, userUUID, courier);
        var tariffLocationSet =
            findLocationsForTariff(idListToCheck, addNewTariffDto.getRegionId())
                .stream().map(location -> TariffLocation.builder()
                    .tariffsInfo(tariffsInfo)
                    .location(location)
                    .locationStatus(LocationStatus.ACTIVE)
                    .build())
                .collect(Collectors.toSet());
        List<Long> existingLocationsIds =
            tariffLocationSet.stream().map(tariffLocation -> tariffLocation.getLocation().getId())
                .collect(Collectors.toList());
        idListToCheck.removeAll(existingLocationsIds);
        tariffsInfo.setTariffLocations(tariffLocationSet);
        tariffsLocationRepository.saveAll(tariffLocationSet);
        tariffsInfoRepository.save(tariffsInfo);
        return new AddNewTariffResponseDto(tariffForLocationAndCourierAlreadyExistIdList, idListToCheck);
    }

    private TariffsInfo createTariff(AddNewTariffDto addNewTariffDto, String userUUID, Courier courier) {
        TariffsInfo tariffsInfo = TariffsInfo.builder()
            .createdAt(LocalDate.now())
            .courier(courier)
            .receivingStationList(findReceivingStationsForTariff(addNewTariffDto.getReceivingStationsIdList()))
            .locationStatus(LocationStatus.NEW)
            .creator(userRepository.findByUuid(userUUID))
            .courierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER)
            .build();
        return tariffsInfoRepository.save(tariffsInfo);
    }

    private List<Long> verifyIfTariffExists(List<Long> locationIds, Long courierId) {
        var tariffLocationListList = tariffsLocationRepository
            .findAllByCourierIdAndLocationIds(courierId, locationIds);
        List<Long> alreadyExistsTariff = tariffLocationListList.stream()
            .map(tariffLocation -> tariffLocation.getLocation().getId())
            .collect(Collectors.toList());
        if (alreadyExistsTariff.stream().anyMatch(locationIds::contains)) {
            throw new TariffAlreadyExistsException(ErrorMessage.TARIFF_IS_ALREADY_EXISTS);
        }
        return alreadyExistsTariff;
    }

    private Courier tryToFindCourier(Long courierId) {
        return courierRepository.findById(courierId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID + courierId));
    }

    @Override
    public boolean checkIfTariffExists(AddNewTariffDto addNewTariffDto) {
        List<TariffLocation> tariffLocations = tariffsLocationRepository.findAllByCourierIdAndLocationIds(
            addNewTariffDto.getCourierId(), addNewTariffDto.getLocationIdList());

        return (!CollectionUtils.isEmpty(tariffLocations));
    }

    @Override
    public void setTariffLimitByAmountOfBags(Long tariffId, EditAmountOfBagDto dto) {
        TariffsInfo tariffsInfo = tryToFindTariffById(tariffId);
        tariffsInfo.setMaxAmountOfBigBags(dto.getMaxAmountOfBigBags());
        tariffsInfo.setMinAmountOfBigBags(dto.getMinAmountOfBigBags());
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG);
        tariffsInfo.setLocationStatus(LocationStatus.ACTIVE);
        tariffsInfoRepository.save(tariffsInfo);
    }

    @Override
    public void setTariffLimitBySumOfOrder(Long tariffId, EditPriceOfOrder dto) {
        TariffsInfo tariffsInfo = tryToFindTariffById(tariffId);
        tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        tariffsInfo.setMaxPriceOfOrder(dto.getMaxPriceOfOrder());
        tariffsInfo.setMinPriceOfOrder(dto.getMinPriceOfOrder());
        tariffsInfo.setLocationStatus(LocationStatus.ACTIVE);
        tariffsInfoRepository.save(tariffsInfo);
    }

    @Override
    public void setTariffLimits(Long tariffId, SetTariffLimitsDto setTariffLimitsDto) {
        TariffsInfo tariffsInfo = tryToFindTariffById(tariffId);

        if (bagRepository.getBagsByTariffsInfoAndMinAmountOfBags(tariffsInfo, MinAmountOfBag.INCLUDE).isEmpty()) {
            throw new BadRequestException(ErrorMessage.BAGS_WITH_MIN_AMOUNT_OF_BIG_BAGS_NOT_FOUND);
        }

        if ((setTariffLimitsDto.getMinAmountOfBigBags() == 0L && setTariffLimitsDto.getMinPriceOfOrder() == 0L)
            || setTariffLimitsDto.getMinAmountOfBigBags() > 0L && setTariffLimitsDto.getMinPriceOfOrder() > 0L) {
            throw new BadRequestException(ErrorMessage.TARIFF_LIMITS_ARE_INPUTTED_INCORRECTLY);
        }

        if (setTariffLimitsDto.getMinAmountOfBigBags() > 0L && setTariffLimitsDto.getMinPriceOfOrder() == 0L) {
            if (setTariffLimitsDto.getMinAmountOfBigBags() > setTariffLimitsDto.getMaxAmountOfBigBags()) {
                throw new BadRequestException(ErrorMessage.MAX_BAG_VALUE_IS_INCORRECT);
            }

            tariffsInfo.setMinPriceOfOrder(null);
            tariffsInfo.setMaxPriceOfOrder(null);

            tariffsInfo.setMinAmountOfBigBags(setTariffLimitsDto.getMinAmountOfBigBags());
            tariffsInfo.setMaxAmountOfBigBags(setTariffLimitsDto.getMaxAmountOfBigBags());
            tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG);
            tariffsInfo.setLocationStatus(LocationStatus.ACTIVE);
        }

        if (setTariffLimitsDto.getMinPriceOfOrder() > 0L && setTariffLimitsDto.getMinAmountOfBigBags() == 0L) {
            if (setTariffLimitsDto.getMinPriceOfOrder() > setTariffLimitsDto.getMaxPriceOfOrder()) {
                throw new BadRequestException(ErrorMessage.MAX_PRICE_VALUE_IS_INCORRECT);
            }

            tariffsInfo.setMinAmountOfBigBags(null);
            tariffsInfo.setMaxAmountOfBigBags(null);

            tariffsInfo.setMinPriceOfOrder(setTariffLimitsDto.getMinPriceOfOrder());
            tariffsInfo.setMaxPriceOfOrder(setTariffLimitsDto.getMaxPriceOfOrder());
            tariffsInfo.setCourierLimit(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
            tariffsInfo.setLocationStatus(LocationStatus.ACTIVE);
        }

        tariffsInfoRepository.save(tariffsInfo);
    }

    @Override
    @Transactional
    public void deactivateTariffCard(Long tariffId) {
        TariffsInfo tariffsInfo = tryToFindTariffById(tariffId);

        var tariffLocations = changeTariffLocationsStatusToDeactivated(
            tariffsInfo.getTariffLocations());

        tariffsInfo.setTariffLocations(tariffLocations);
        tariffsInfo.setLocationStatus(LocationStatus.DEACTIVATED);

        tariffsInfoRepository.save(tariffsInfo);
    }

    private Set<TariffLocation> changeTariffLocationsStatusToDeactivated(Set<TariffLocation> tariffLocations) {
        return tariffLocations.stream()
            .map(this::deactivateTariffLocation)
            .collect(Collectors.toSet());
    }

    private TariffLocation deactivateTariffLocation(TariffLocation tariffLocation) {
        tariffLocation.setLocationStatus(LocationStatus.DEACTIVATED);
        return tariffLocation;
    }

    @Override
    public void editLocations(List<EditLocationDto> editLocationDtoList) {
        editLocationDtoList.forEach(this::editLocation);
    }

    private void editLocation(EditLocationDto editLocationDto) {
        Location location = tryToFindLocationById(editLocationDto.getLocationId());
        if (!locationExists(editLocationDto.getNameUa(), editLocationDto.getNameEn(), location.getRegion())) {
            location.setNameUk(editLocationDto.getNameUa());
            location.setNameEn(editLocationDto.getNameEn());
            locationRepository.save(location);
        }
    }

    private boolean locationExists(String nameUk, String nameEn, Region region) {
        return locationRepository.existsByNameUkAndNameEnAndRegion(nameUk, nameEn, region);
    }

    @Override
    @Transactional
    public void changeTariffLocationsStatus(Long tariffId, ChangeTariffLocationStatusDto dto, String param) {
        tryToFindTariffById(tariffId);
        if ("activate".equalsIgnoreCase(param)) {
            tariffsLocationRepository.changeStatusAll(tariffId, dto.getLocationIds(), LocationStatus.ACTIVE.name());
        } else if ("deactivate".equalsIgnoreCase(param)) {
            tariffsLocationRepository.changeStatusAll(tariffId, dto.getLocationIds(),
                LocationStatus.DEACTIVATED.name());
        } else {
            throw new BadRequestException("Unresolvable param");
        }
    }

    @Override
    @Transactional
    public void deactivateTariffForChosenParam(DetailsOfDeactivateTariffsDto details) {
        if (shouldDeactivateTariffsByRegions(details)) {
            deactivateTariffsForChosenParamRepository.deactivateTariffsByRegions(details.getRegionsId().get());
        } else if (shouldDeactivateTariffsByRegionsAndCities(details)) {
            deactivateTariffsForChosenParamRepository.deactivateTariffsByRegionsAndCities(details.getCitiesId().get(),
                details.getRegionsId().get().get(0));
        } else if (shouldDeactivateTariffsByCourier(details)) {
            deactivateTariffsForChosenParamRepository.deactivateTariffsByCourier(details.getCourierId().get());
        } else if (shouldDeactivateTariffsByReceivingStations(details)) {
            deactivateTariffsForChosenParamRepository.deactivateTariffsByReceivingStations(
                details.getStationsId().get());
        } else if (shouldDeactivateTariffsByCourierAndReceivingStations(details)) {
            deactivateTariffsForChosenParamRepository.deactivateTariffsByCourierAndReceivingStations(
                details.getCourierId().get(), details.getStationsId().get());
        } else if (shouldDeactivateTariffsByCourierAndRegion(details)) {
            deactivateTariffsForChosenParamRepository.deactivateTariffsByCourierAndRegion(
                details.getRegionsId().get().get(0), details.getCourierId().get());
        } else if (shouldDeactivateTariffsByRegionAndCityAndStation(details)) {
            deactivateTariffsForChosenParamRepository.deactivateTariffsByRegionAndCitiesAndStations(
                details.getRegionsId().get().get(0), details.getCitiesId().get(), details.getStationsId().get());
        } else if (shouldDeactivateTariffsByAll(details)) {
            deactivateTariffsForChosenParamRepository.deactivateTariffsByAllParam(
                details.getRegionsId().get().get(0), details.getCitiesId().get(),
                details.getStationsId().get(), details.getCourierId().get());
        } else {
            throw new BadRequestException("Bad request. Please choose another combination of parameters");
        }
    }

    /**
     * Method that checks if the tariff should be deactivated by details. In this
     * case size of RegionsList should be one because we choose more than one param.
     *
     * @param details - contains regions id, cities id, receiving stations id and
     *                courier id.
     * @return true if you have to deactivate tariff by details and false if not.
     * @author Nikita Korzh.
     */
    private boolean shouldDeactivateTariffsByAll(DetailsOfDeactivateTariffsDto details) {
        if (details.getRegionsId().isPresent() && details.getCitiesId().isPresent()
            && details.getStationsId().isPresent() && details.getCourierId().isPresent()) {
            if (details.getRegionsId().get().size() == 1) {
                if (regionRepository.existsRegionById(details.getRegionsId().get().get(0))
                    && deactivateTariffsForChosenParamRepository.isCitiesExistForRegion(details.getCitiesId().get(),
                        details.getRegionsId().get().get(0))
                    && deactivateTariffsForChosenParamRepository
                        .isReceivingStationsExists(details.getStationsId().get())
                    && courierRepository.existsCourierById(details.getCourierId().get())) {
                    return true;
                } else {
                    throw new NotFoundException(String.format(
                        REGION_OR_CITIES_OR_RECEIVING_STATIONS_OR_COURIER_EXIST_MESSAGE,
                        details.getRegionsId().get(), details.getCitiesId().get(),
                        details.getStationsId().get(), details.getCourierId().get()));
                }
            } else {
                throw new BadRequestException(BAD_SIZE_OF_REGIONS_MESSAGE);
            }
        }
        return false;
    }

    /**
     * Method that checks if the tariff should be deactivated by region id, cities
     * id and receiving stations. In this case size of RegionsList should be one
     * because we choose more than one param.
     *
     * @param details - contains regions id, cities id, receiving stations id and
     *                courier id.
     * @return true if you have to deactivate tariff by region id, cities id and
     *         receiving stations and false if not.
     * @author Nikita Korzh.
     */
    private boolean shouldDeactivateTariffsByRegionAndCityAndStation(DetailsOfDeactivateTariffsDto details) {
        if (details.getRegionsId().isPresent() && details.getCitiesId().isPresent()
            && details.getStationsId().isPresent() && details.getCourierId().isEmpty()) {
            if (details.getRegionsId().get().size() == 1) {
                if (regionRepository.existsRegionById(details.getRegionsId().get().get(0))
                    && deactivateTariffsForChosenParamRepository
                        .isCitiesExistForRegion(details.getCitiesId().get(),
                            details.getRegionsId().get().get(0))
                    && deactivateTariffsForChosenParamRepository
                        .isReceivingStationsExists(details.getStationsId().get())) {
                    return true;
                } else {
                    throw new NotFoundException(String.format(REGION_OR_CITIES_OR_RECEIVING_STATIONS_EXIST_MESSAGE,
                        details.getRegionsId().get(), details.getCitiesId().get(), details.getStationsId().get()));
                }
            } else {
                throw new BadRequestException(BAD_SIZE_OF_REGIONS_MESSAGE);
            }
        }
        return false;
    }

    /**
     * Method that checks if the tariff should be deactivated by region id and
     * courier id. In this case size of RegionsList should be one because we choose
     * more than one param.
     *
     * @param details - contains regions id, cities id, receiving stations id and
     *                courier id.
     * @return true if you have to deactivate tariff by region id and courier id and
     *         false if not.
     * @author Nikita Korzh.
     */
    private boolean shouldDeactivateTariffsByCourierAndRegion(DetailsOfDeactivateTariffsDto details) {
        if (details.getRegionsId().isPresent() && details.getCourierId().isPresent()
            && details.getCitiesId().isEmpty() && details.getStationsId().isEmpty()) {
            if (details.getRegionsId().get().size() == 1) {
                if (regionRepository.existsRegionById(details.getRegionsId().get().get(0))
                    && courierRepository.existsCourierById(details.getCourierId().get())) {
                    return true;
                } else {
                    throw new NotFoundException(String.format(REGION_OR_COURIER_EXIST_MESSAGE,
                        details.getRegionsId().get(), details.getCourierId().get()));
                }
            } else {
                throw new BadRequestException(BAD_SIZE_OF_REGIONS_MESSAGE);
            }
        }
        return false;
    }

    /**
     * Method that checks if the tariff should be deactivated by courier id and
     * receiving stations id.
     *
     * @param details - contains regions id, cities id, receiving stations id and
     *                courier id.
     * @return true if you have to deactivate tariff by region id and courier id and
     *         false if not.
     * @author Nikita Korzh.
     */
    private boolean shouldDeactivateTariffsByCourierAndReceivingStations(DetailsOfDeactivateTariffsDto details) {
        if (details.getStationsId().isPresent() && details.getCourierId().isPresent()
            && details.getRegionsId().isEmpty() && details.getCitiesId().isEmpty()) {
            if (courierRepository.existsCourierById(details.getCourierId().get())
                && deactivateTariffsForChosenParamRepository
                    .isReceivingStationsExists(details.getStationsId().get())) {
                return true;
            } else {
                throw new NotFoundException(String.format(RECEIVING_STATIONS_OR_COURIER_EXIST_MESSAGE,
                    details.getStationsId().get(), details.getCourierId().get()));
            }
        }
        return false;
    }

    /**
     * Method that checks if the tariff should be deactivated by receiving stations
     * id.
     *
     * @param details - contains regions id, cities id, receiving stations id and
     *                courier id.
     * @return true if you have to deactivate tariff by receiving stations and false
     *         if not.
     * @author Nikita Korzh.
     */
    private boolean shouldDeactivateTariffsByReceivingStations(DetailsOfDeactivateTariffsDto details) {
        if (details.getStationsId().isPresent() && details.getRegionsId().isEmpty()
            && details.getCitiesId().isEmpty() && details.getCourierId().isEmpty()) {
            if (deactivateTariffsForChosenParamRepository
                .isReceivingStationsExists(details.getStationsId().get())) {
                return true;
            } else {
                throw new NotFoundException(String.format(RECEIVING_STATIONS_EXIST_MESSAGE,
                    details.getStationsId().get()));
            }
        }
        return false;
    }

    /**
     * Method that checks if the tariff should be deactivated by courier id.
     *
     * @param details - contains regions id, cities id, receiving stations id and
     *                courier id.
     * @return true if you have to deactivate tariff by courier id and false if not.
     * @author Nikita Korzh.
     */
    private boolean shouldDeactivateTariffsByCourier(DetailsOfDeactivateTariffsDto details) {
        if (details.getCourierId().isPresent() && details.getRegionsId().isEmpty()
            && details.getCitiesId().isEmpty() && details.getStationsId().isEmpty()) {
            if (courierRepository.existsCourierById(details.getCourierId().get())) {
                return true;
            } else {
                throw new NotFoundException(String.format(COURIER_EXISTS_MESSAGE, details.getCourierId().get()));
            }
        }
        return false;
    }

    /**
     * Method that checks if the tariff should be deactivated by region id and
     * cities id. In this case size of RegionsList should be one because we choose
     * more than one param.
     *
     * @param details - contains regions id, cities id, receiving stations id and
     *                courier id.
     * @return true if you have to deactivate tariff by region id and cities id and
     *         false if not.
     * @author Nikita Korzh.
     */
    private boolean shouldDeactivateTariffsByRegionsAndCities(DetailsOfDeactivateTariffsDto details) {
        if (details.getRegionsId().isPresent() && details.getCitiesId().isPresent()
            && details.getStationsId().isEmpty() && details.getCourierId().isEmpty()) {
            if (details.getRegionsId().get().size() == 1) {
                if (regionRepository.existsRegionById(details.getRegionsId().get().get(0))
                    && deactivateTariffsForChosenParamRepository.isCitiesExistForRegion(details.getCitiesId().get(),
                        details.getRegionsId().get().get(0))) {
                    return true;
                } else {
                    throw new NotFoundException(String.format(REGIONS_OR_CITIES_EXIST_MESSAGE,
                        details.getRegionsId().get(), details.getCitiesId().get()));
                }
            } else {
                throw new BadRequestException(BAD_SIZE_OF_REGIONS_MESSAGE);
            }
        }
        return false;
    }

    /**
     * Method that checks if the tariff should be deactivated by region id.
     *
     * @param details - contains regions id, cities id, receiving stations id and
     *                courier id.
     * @return true if you have to deactivate tariff by region id and false if not.
     * @author Nikita Korzh.
     */
    private boolean shouldDeactivateTariffsByRegions(DetailsOfDeactivateTariffsDto details) {
        if (details.getRegionsId().isPresent() && details.getCitiesId().isEmpty()
            && details.getStationsId().isEmpty() && details.getCourierId().isEmpty()) {
            if (deactivateTariffsForChosenParamRepository.isRegionsExists(details.getRegionsId().get())) {
                return true;
            } else {
                throw new NotFoundException(String.format(REGIONS_EXIST_MESSAGE, details.getRegionsId().get()));
            }
        }
        return false;
    }
}
