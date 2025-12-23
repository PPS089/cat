     @RestController
     public class PetsController {

         private final PetsService petsService;

         @Autowired
         public PetsController(PetsService petsService) {
             this.petsService = petsService;
         }
     }
     