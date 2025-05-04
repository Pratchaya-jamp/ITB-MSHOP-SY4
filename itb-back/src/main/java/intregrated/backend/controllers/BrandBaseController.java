//package intregrated.backend.controllers;
//
//import intregrated.backend.entities.BrandBase;
//import intregrated.backend.services.BrandBaseService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/v1/sale-items")
//public class BrandBaseController {
//    @Autowired
//    BrandBaseService brandBaseService;
//
//    @GetMapping("")
//    public List<BrandBase> getAllBrandBases() {
//        return brandBaseService.getAllBrandBase();
//    }
//
//    @GetMapping("/{id}")
//    public BrandBase getBrandBaseById(@PathVariable Integer id) {
//        return brandBaseService.getBrandBaseById(id);
//    }
//}
