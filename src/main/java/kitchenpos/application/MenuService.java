package kitchenpos.application;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kitchenpos.dao.MenuDao;
import kitchenpos.dao.MenuGroupDao;
import kitchenpos.dao.MenuProductDao;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Product;
import kitchenpos.dto.MenuCreateRequestDto;
import kitchenpos.dto.MenuProductCreateRequestDto;
import kitchenpos.dto.MenuResponseDto;
import kitchenpos.repository.ProductRepository;

@Service
public class MenuService {
    private final MenuDao menuDao;
    private final MenuGroupDao menuGroupDao;
    private final MenuProductDao menuProductDao;
    private final ProductRepository productRepository;

    public MenuService(
        final MenuDao menuDao,
        final MenuGroupDao menuGroupDao,
        final MenuProductDao menuProductDao,
        final ProductRepository productRepository
    ) {
        this.menuDao = menuDao;
        this.menuGroupDao = menuGroupDao;
        this.menuProductDao = menuProductDao;
        this.productRepository = productRepository;
    }

    @Transactional
    public MenuResponseDto create(final MenuCreateRequestDto menuCreateRequest) {
        if (!menuGroupDao.existsById(menuCreateRequest.getMenuGroupId())) {
            throw new IllegalArgumentException("존재하지 않는 메뉴그룹입니다.");
        }

        final List<MenuProductCreateRequestDto> menuProductCreateRequests = menuCreateRequest.getMenuProductRequestDto();

        BigDecimal sum = BigDecimal.ZERO;
        for (final MenuProductCreateRequestDto menuProductCreateRequest : menuProductCreateRequests) {
            if (Objects.isNull(menuProductCreateRequest.getProductId())) {
                throw new IllegalArgumentException("존재하지 않는 메뉴는 등록할 수 없습니다.");
            }
            final Product product = productRepository.findById(menuProductCreateRequest.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴는 등록할 수 없습니다."));
            sum = sum.add(product.getPrice().multiply(BigDecimal.valueOf(menuProductCreateRequest.getQuantity())));
        }

        final BigDecimal price = menuCreateRequest.getPrice();
        if (price.compareTo(sum) > 0) {
            throw new IllegalArgumentException("메뉴의 가격은 메뉴 상품 가격의 합보다 작거나 같아야 합니다.");
        }

        final Menu savedMenu = menuDao.save(menuCreateRequest.toEntity());

        final Long menuId = savedMenu.getId();
        final List<MenuProduct> savedMenuProducts = new ArrayList<>();
        for (final MenuProductCreateRequestDto menuProductCreateRequest : menuProductCreateRequests) {
            MenuProduct menuProduct = menuProductCreateRequest.toEntity(menuId);
            savedMenuProducts.add(menuProductDao.save(menuProduct));
        }

        return MenuResponseDto.from(savedMenu, savedMenuProducts);
    }

    public List<MenuResponseDto> list() {
        final List<Menu> menus = menuDao.findAll();

        List<MenuResponseDto> menuResponses = new ArrayList<>();
        for (final Menu menu : menus) {
            List<MenuProduct> menuProducts = menuProductDao.findAllByMenuId(menu.getId());
            menuResponses.add(MenuResponseDto.from(menu, menuProducts));
        }
        return menuResponses;
    }
}
