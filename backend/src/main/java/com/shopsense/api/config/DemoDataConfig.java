package com.shopsense.api.config;

import com.shopsense.api.entity.Category;
import com.shopsense.api.entity.Product;
import com.shopsense.api.entity.Role;
import com.shopsense.api.entity.User;
import com.shopsense.api.repository.CategoryRepository;
import com.shopsense.api.repository.ProductRepository;
import com.shopsense.api.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Configuration
public class DemoDataConfig {

    @Bean
    CommandLineRunner seedDemoData(
            CategoryRepository categories,
            ProductRepository products,
            UserRepository users,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            seedUser(
                    users,
                    passwordEncoder,
                    "demo@shopsense.ai",
                    "Demo",
                    "User",
                    "DemoPass!23",
                    Role.CUSTOMER
            );

            seedUser(
                    users,
                    passwordEncoder,
                    "admin@shopsense.ai",
                    "ShopSense",
                    "Admin",
                    "AdminPass!23",
                    Role.ADMIN
            );

            Map<String, String> categoryNames = Map.ofEntries(
                    Map.entry("electronics", "Electronics"),
                    Map.entry("home-kitchen", "Home & Kitchen"),
                    Map.entry("groceries", "Groceries"),
                    Map.entry("fashion", "Fashion"),
                    Map.entry("beauty", "Beauty & Personal Care"),
                    Map.entry("sports-fitness", "Sports & Fitness"),
                    Map.entry("travel", "Travel & Luggage"),
                    Map.entry("furniture-decor", "Furniture & Decor"),
                    Map.entry("gaming", "Gaming"),
                    Map.entry("books-stationery", "Books & Stationery"),
                    Map.entry("automotive", "Automotive"),
                    Map.entry("pet-supplies", "Pet Supplies")
            );

            Map<String, Category> saved = new HashMap<>();

            categoryNames.forEach((slug, name) -> {
                Category category = categories.findBySlug(slug)
                        .orElseGet(() -> {
                            Category c = new Category();
                            c.setName(name);
                            c.setSlug(slug);
                            c.setDescription(
                                    "Thoughtful "
                                            + name.toLowerCase()
                                            + " picks curated by ShopSense AI."
                            );
                            return categories.save(c);
                        });

                saved.put(slug, category);
            });

            /*
             * MIGRATE OLD CATEGORY DATA
             *
             * These older slugs existed in previous versions.
             * Move their products into the new canonical 12-category structure,
             * then remove the legacy category.
             */
            migrateLegacyCategory(
                    categories,
                    products,
                    saved,
                    "running",
                    "sports-fitness"
            );

            migrateLegacyCategory(
                    categories,
                    products,
                    saved,
                    "outdoor",
                    "travel"
            );

            migrateLegacyCategory(
                    categories,
                    products,
                    saved,
                    "home",
                    "furniture-decor"
            );

            migrateLegacyCategory(
                    categories,
                    products,
                    saved,
                    "accessories",
                    "fashion"
            );

            seedCoreProducts(products, saved);

            seedElectronics(products, saved.get("electronics"));
            seedHomeKitchen(products, saved.get("home-kitchen"));
            seedGroceries(products, saved.get("groceries"));
            seedFashion(products, saved.get("fashion"));
            seedBeauty(products, saved.get("beauty"));
            seedSports(products, saved.get("sports-fitness"));
            seedTravel(products, saved.get("travel"));
            seedFurniture(products, saved.get("furniture-decor"));
            seedGaming(products, saved.get("gaming"));
            seedBooks(products, saved.get("books-stationery"));
            seedAutomotive(products, saved.get("automotive"));
            seedPets(products, saved.get("pet-supplies"));
            seedCompleteTypeCoverage(products, saved);
        };
    }

    // -------------------------------------------------------------------------
    // USERS
    // -------------------------------------------------------------------------

    private void seedUser(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            String email,
            String firstName,
            String lastName,
            String password,
            Role role
    ) {
        users.findByEmailIgnoreCase(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRole(role);
            return users.save(user);
        });
    }

    // -------------------------------------------------------------------------
    // LEGACY CATEGORY MIGRATION
    // -------------------------------------------------------------------------

    private void migrateLegacyCategory(
            CategoryRepository categories,
            ProductRepository products,
            Map<String, Category> canonical,
            String legacySlug,
            String canonicalSlug
    ) {
        categories.findBySlug(legacySlug).ifPresent(legacy -> {

            Category target = canonical.get(canonicalSlug);

            products.findByCategory(legacy).forEach(product -> {
                product.setCategory(target);
                products.save(product);
            });

                        UUID legacyId = legacy.getId();
                        if (legacyId != null) {
                                categories.deleteById(legacyId);
                        }
        });
    }

    // -------------------------------------------------------------------------
    // CORE PRODUCTS
    // -------------------------------------------------------------------------

    private void seedCoreProducts(
            ProductRepository products,
            Map<String, Category> categories
    ) {

        product(
                products,
                categories.get("electronics"),
                "Sony WH-1000XM5",
                "sony-wh-1000xm5",
                "Sony",
                "Premium wireless noise cancelling headphones for travel and focused listening.",
                "Flagship wireless noise cancelling headphones.",
                34990,
                29990,
                "ELE-SON-WH1000XM5",
                15,
                "Aluminium",
                "Black",
                "electronics,headphones,wireless,noise-cancelling,travel",
                "Headphones",
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products,
                categories.get("electronics"),
                "Bose QuietComfort",
                "bose-quietcomfort",
                "Bose",
                "Comfortable wireless headphones with active noise cancellation.",
                "Comfort-first wireless headphones.",
                32990,
                28990,
                "ELE-BOS-QC01",
                14,
                "Polycarbonate",
                "Black",
                "electronics,headphones,wireless,noise-cancelling",
                "Headphones",
                "https://images.unsplash.com/photo-1484704849700-f032a568e944?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products,
                categories.get("electronics"),
                "JBL Live 660NC",
                "jbl-live-660nc",
                "JBL",
                "Wireless over-ear headphones with active noise cancellation.",
                "Wireless JBL headphones for work and travel.",
                11999,
                8999,
                "ELE-JBL-660NC",
                22,
                "Plastic",
                "Blue",
                "electronics,headphones,jbl,wireless,anc",
                "Headphones",
                "https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products,
                categories.get("electronics"),
                "Apple AirPods Pro",
                "apple-airpods-pro",
                "Apple",
                "Compact true wireless earbuds with active noise cancellation.",
                "Premium wireless earbuds for everyday use.",
                24900,
                21900,
                "ELE-APP-APPRO",
                20,
                "Polycarbonate",
                "White",
                "electronics,earbuds,wireless,apple",
                "Earbuds",
                "https://images.unsplash.com/photo-1606220945770-b5b6c2c55bf1?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products,
                categories.get("electronics"),
                "Samsung Galaxy Buds",
                "samsung-galaxy-buds",
                "Samsung",
                "Wireless earbuds with compact charging case and balanced sound.",
                "Compact Samsung wireless earbuds.",
                12999,
                9999,
                "ELE-SAM-BUDS",
                25,
                "Plastic",
                "Graphite",
                "electronics,earbuds,samsung,wireless",
                "Earbuds",
                "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products,
                categories.get("electronics"),
                "OnePlus Buds Pro",
                "oneplus-buds-pro",
                "OnePlus",
                "Wireless earbuds designed for everyday listening and calls.",
                "Everyday true wireless earbuds.",
                9999,
                7999,
                "ELE-ONE-BUDSPRO",
                28,
                "Plastic",
                "Black",
                "electronics,earbuds,oneplus,wireless",
                "Earbuds",
                "https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products,
                categories.get("electronics"),
                "Samsung Galaxy S Series",
                "samsung-galaxy-s-series",
                "Samsung",
                "A premium Android smartphone with a high-resolution display and modern camera system.",
                "Premium Samsung smartphone.",
                69999,
                62999,
                "ELE-SAM-SERIES",
                12,
                "Aluminium",
                "Graphite",
                "electronics,smartphone,mobile,samsung,android",
                "Smartphones",
                "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products,
                categories.get("electronics"),
                "Google Pixel",
                "google-pixel",
                "Google",
                "Clean Android smartphone with an advanced camera experience.",
                "Google Pixel smartphone.",
                64999,
                59999,
                "ELE-GOO-PIXEL",
                11,
                "Aluminium",
                "Obsidian",
                "electronics,smartphone,mobile,google,android",
                "Smartphones",
                "https://images.unsplash.com/photo-1598327105666-5b89351f4f6?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products,
                categories.get("electronics"),
                "Dell Inspiron Laptop",
                "dell-inspiron-laptop",
                "Dell",
                "Reliable laptop for study, productivity, browsing, and everyday work.",
                "Everyday Dell productivity laptop.",
                64990,
                59990,
                "ELE-DEL-INSP",
                9,
                "Aluminium",
                "Silver",
                "electronics,laptop,dell,computer,work",
                "Laptops",
                "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products,
                categories.get("electronics"),
                "ASUS VivoBook",
                "asus-vivobook",
                "ASUS",
                "Slim laptop for study, office work, and everyday productivity.",
                "Slim ASUS everyday laptop.",
                59990,
                54990,
                "ELE-ASU-VIVO",
                10,
                "Aluminium",
                "Silver",
                "electronics,laptop,asus,computer,work",
                "Laptops",
                "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products,
                categories.get("electronics"),
                "Logitech MX Keys",
                "logitech-mx-keys",
                "Logitech",
                "Low-profile wireless keyboard for focused desk setups.",
                "Premium wireless desktop keyboard.",
                12995,
                9995,
                "ELE-LOG-MXKEYS",
                18,
                "Aluminium",
                "Graphite",
                "electronics,keyboards,logitech,wireless,desk",
                "Keyboards",
                "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products,
                categories.get("electronics"),
                "Razer BlackWidow",
                "razer-blackwidow",
                "Razer",
                "Mechanical gaming keyboard with responsive switches.",
                "Mechanical gaming keyboard.",
                13999,
                10999,
                "ELE-RAZ-BLACKWIDOW",
                20,
                "Aluminium",
                "Black",
                "electronics,keyboards,gaming,razer,mechanical",
                "Keyboards",
                "https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products,
                categories.get("gaming"),
                "Razer BlackWidow V3",
                "razer-blackwidow-v3",
                "Razer",
                "Mechanical gaming keyboard designed for fast and consistent input.",
                "Mechanical keyboard for gaming.",
                14999,
                11999,
                "GAM-RAZ-BWV3",
                14,
                "Aluminium",
                "Black",
                "gaming,keyboards,razer,mechanical",
                "Gaming Keyboards",
                "https://images.unsplash.com/photo-1541140532154-b024d705b90a?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products,
                categories.get("sports-fitness"),
                "Nike Air Zoom Runner",
                "nike-air-zoom-runner",
                "Nike",
                "Cushioned running shoe designed for daily road running.",
                "Daily running shoe with responsive cushioning.",
                8999,
                7499,
                "SPO-NIK-AZR",
                24,
                "Mesh",
                "Black",
                "sports,running-shoes,nike,running,fitness",
                "Running Shoes",
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products,
                categories.get("travel"),
                "Osprey Daypack",
                "osprey-daypack",
                "Osprey",
                "Durable everyday backpack for commuting and short travel.",
                "Practical everyday travel backpack.",
                6999,
                5999,
                "TRV-OSP-DAY",
                18,
                "Recycled nylon",
                "Black",
                "travel,backpacks,osprey,commute",
                "Backpacks",
                "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products,
                categories.get("beauty"),
                "The Ordinary Niacinamide Serum",
                "the-ordinary-niacinamide-serum",
                "The Ordinary",
                "Lightweight facial serum for everyday skincare routines.",
                "Simple daily skincare serum.",
                999,
                899,
                "BEA-ORD-NIA",
                30,
                "Water-based",
                "Clear",
                "beauty,serums,skincare,face",
                "Serums",
                "https://images.unsplash.com/photo-1571781926291-c477ebfd024b?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products,
                categories.get("home-kitchen"),
                "Philips Air Fryer",
                "philips-air-fryer",
                "Philips",
                "Compact air fryer for convenient everyday cooking.",
                "Countertop air fryer for everyday meals.",
                8999,
                7499,
                "HOM-PHI-AIR",
                16,
                "Steel",
                "Black",
                "home-kitchen,air-fryers,kitchen,appliance",
                "Air Fryers",
                "https://images.unsplash.com/photo-1585515320310-259814833e62?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products,
                categories.get("books-stationery"),
                "Leuchtturm1917 Notebook",
                "leuchtturm1917-notebook",
                "Leuchtturm1917",
                "Premium hardcover notebook for notes, journals, and planning.",
                "Premium everyday notebook.",
                1999,
                1699,
                "BOK-LEU-NOTE",
                35,
                "Paper",
                "Black",
                "books-stationery,notebooks,journals,writing",
                "Notebooks",
                "https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products,
                categories.get("automotive"),
                "Spigen Car Phone Mount",
                "spigen-car-phone-mount",
                "Spigen",
                "Secure adjustable smartphone mount for vehicle dashboards.",
                "Stable in-car smartphone holder.",
                2299,
                1899,
                "AUT-SPI-MOUNT",
                26,
                "ABS plastic",
                "Black",
                "automotive,phone-mount,car,accessories",
                "Car Phone Mounts",
                "https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products,
                categories.get("pet-supplies"),
                "K&H Pet Bed",
                "kh-pet-bed",
                "K&H",
                "Soft washable pet bed designed for comfortable naps.",
                "Comfortable washable pet bed.",
                2999,
                2499,
                "PET-KH-BED",
                17,
                "Cotton",
                "Grey",
                "pets,beds,dog,cat,pet-supplies",
                "Pet Beds",
                "https://images.unsplash.com/photo-1583337130417-3346a1be7dee?auto=format&fit=crop&w=1200&q=90"
        );
    }

    // -------------------------------------------------------------------------
    // CATEGORY SEEDS
    // -------------------------------------------------------------------------

    private void seedElectronics(ProductRepository products, Category category) {

        product(
                products, category,
                "Sony WH-CH720N",
                "sony-wh-ch720n",
                "Sony",
                "Lightweight wireless headphones with noise cancellation.",
                "Lightweight Sony wireless headphones.",
                9990, 8490,
                "ELE-SON-CH720N",
                16,
                "Plastic",
                "Black",
                "electronics,headphones,sony,wireless",
                "Headphones",
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Bose QuietComfort Ultra",
                "bose-quietcomfort-ultra",
                "Bose",
                "Premium over-ear headphones with active noise cancellation.",
                "Premium Bose noise cancelling headphones.",
                42900, 37900,
                "ELE-BOS-UQ",
                10,
                "Aluminium",
                "Black",
                "electronics,headphones,bose,wireless,anc",
                "Headphones",
                "https://images.unsplash.com/photo-1484704849700-f032a568e944?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "JBL Tune 770NC",
                "jbl-tune-770nc",
                "JBL",
                "Wireless noise cancelling headphones for everyday listening.",
                "Affordable JBL noise cancelling headphones.",
                7999, 6499,
                "ELE-JBL-770NC",
                24,
                "Plastic",
                "Blue",
                "electronics,headphones,jbl,wireless,anc",
                "Headphones",
                "https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Apple AirPods 3rd Gen",
                "apple-airpods-3rd-gen",
                "Apple",
                "Open-fit true wireless earbuds designed for everyday listening.",
                "Everyday Apple wireless earbuds.",
                19900, 16900,
                "ELE-APP-AP3",
                20,
                "Plastic",
                "White",
                "electronics,earbuds,apple,wireless",
                "Earbuds",
                "https://images.unsplash.com/photo-1606220945770-b5b6c2c55bf1?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "OnePlus Buds 3",
                "oneplus-buds-3",
                "OnePlus",
                "Balanced wireless earbuds with strong battery life.",
                "Value-focused OnePlus earbuds.",
                5499, 4499,
                "ELE-ONE-BUDS3",
                35,
                "Plastic",
                "Black",
                "electronics,earbuds,oneplus,wireless",
                "Earbuds",
                "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Nothing Ear",
                "nothing-ear",
                "Nothing",
                "Transparent-design wireless earbuds with active noise cancellation.",
                "Distinctive wireless earbuds.",
                8999, 7999,
                "ELE-NOT-EAR",
                18,
                "Plastic",
                "White",
                "electronics,earbuds,nothing,wireless,anc",
                "Earbuds",
                "https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Apple MacBook Air",
                "apple-macbook-air",
                "Apple",
                "Thin lightweight laptop for study, work, and creative tasks.",
                "Premium lightweight Apple laptop.",
                114900, 104900,
                "ELE-APP-MBA",
                8,
                "Aluminium",
                "Silver",
                "electronics,laptops,apple,computer,work",
                "Laptops",
                "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Lenovo IdeaPad Slim",
                "lenovo-ideapad-slim",
                "Lenovo",
                "Slim productivity laptop for students and office work.",
                "Affordable Lenovo productivity laptop.",
                64990, 57990,
                "ELE-LEN-IDEA",
                12,
                "Aluminium",
                "Silver",
                "electronics,laptops,lenovo,computer,work",
                "Laptops",
                "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Logitech MX Master 3S",
                "logitech-mx-master-3s",
                "Logitech",
                "Premium wireless productivity mouse with ergonomic design.",
                "Professional ergonomic wireless mouse.",
                9995, 8495,
                "ELE-LOG-MX3S",
                19,
                "Recycled plastic",
                "Black",
                "electronics,mice,logitech,wireless,productivity",
                "Mice",
                "https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Razer DeathAdder V3",
                "razer-deathadder-v3",
                "Razer",
                "Lightweight ergonomic gaming mouse for fast competitive play.",
                "Competitive gaming mouse.",
                6999, 5999,
                "ELE-RAZ-DAV3",
                22,
                "Plastic",
                "Black",
                "electronics,mice,gaming,razer",
                "Mice",
                "https://images.unsplash.com/photo-1563297007-0686b7003af7?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "JBL Charge 5",
                "jbl-charge-5",
                "JBL",
                "Portable waterproof Bluetooth speaker with strong battery life.",
                "Portable JBL Bluetooth speaker.",
                17999, 14999,
                "ELE-JBL-CHARGE5",
                15,
                "Plastic",
                "Black",
                "electronics,speakers,jbl,bluetooth,portable",
                "Speakers",
                "https://images.unsplash.com/photo-1589003077984-894e133dabab?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Canon EOS R50",
                "canon-eos-r50",
                "Canon",
                "Compact mirrorless camera for creators and everyday photography.",
                "Compact Canon mirrorless camera.",
                74990, 68990,
                "ELE-CAN-R50",
                7,
                "Magnesium alloy",
                "Black",
                "electronics,cameras,canon,mirrorless,photography",
                "Cameras",
                "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Dell UltraSharp Monitor",
                "dell-ultrasharp-monitor",
                "Dell",
                "High-quality monitor designed for productivity and creative work.",
                "Premium Dell monitor for work.",
                32990, 29990,
                "ELE-DEL-US",
                12,
                "Aluminium",
                "Black",
                "electronics,monitors,dell,display,desk",
                "Monitors",
                "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Apple Watch Series",
                "apple-watch-series",
                "Apple",
                "Smartwatch with fitness tracking, notifications, and daily health features.",
                "Premium Apple smartwatch.",
                45900, 41900,
                "ELE-APP-WATCH",
                12,
                "Aluminium",
                "Midnight",
                "electronics,smartwatches,apple,fitness",
                "Smartwatches",
                "https://images.unsplash.com/photo-1546868871-7041f2a55e12?auto=format&fit=crop&w=1200&q=90"
        );
    }

    // -------------------------------------------------------------------------
    // OTHER CATEGORIES
    // -------------------------------------------------------------------------

    private void seedHomeKitchen(ProductRepository products, Category category) {

        product(
                products, category,
                "Le Creuset Cast Iron Cookware",
                "le-creuset-cast-iron-cookware",
                "Le Creuset",
                "Durable enameled cast iron cookware for everyday cooking.",
                "Premium cast iron cookware.",
                24999, 21999,
                "HOM-LCR-001",
                10,
                "Cast iron",
                "Red",
                "home-kitchen,cookware,le-creuset,kitchen",
                "Cookware",
                "https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "DeLonghi Coffee Maker",
                "delonghi-coffee-maker",
                "DeLonghi",
                "Compact coffee maker for espresso-style drinks at home.",
                "Home coffee maker.",
                16999, 14999,
                "HOM-DEL-002",
                12,
                "Steel",
                "Black",
                "home-kitchen,coffee-makers,delonghi,coffee",
                "Coffee Makers",
                "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Philips Air Fryer",
                "philips-air-fryer-2",
                "Philips",
                "Compact air fryer for convenient everyday meals.",
                "Popular kitchen air fryer.",
                8999, 7499,
                "HOM-PHI-003",
                16,
                "Steel",
                "Black",
                "home-kitchen,air-fryers,philips,kitchen",
                "Air Fryers",
                "https://images.unsplash.com/photo-1585515320310-259814833e62?auto=format&fit=crop&w=1200&q=90"
        );
    }

    private void seedGroceries(ProductRepository products, Category category) {

        product(
                products, category,
                "India Gate Basmati Rice",
                "india-gate-basmati-rice",
                "India Gate",
                "Long-grain basmati rice for everyday home cooking.",
                "Everyday premium basmati rice.",
                999, 899,
                "GRO-IND-001",
                60,
                "Rice",
                "White",
                "groceries,rice,grains,india-gate",
                "Rice & Grains",
                "https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Davidoff Coffee",
                "davidoff-coffee",
                "Davidoff",
                "Rich roasted coffee for a balanced everyday cup.",
                "Premium roasted coffee.",
                799, 699,
                "GRO-DAV-002",
                45,
                "Arabica",
                "Brown",
                "groceries,coffee,davidoff,beverages",
                "Coffee",
                "https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Tata Tea Gold",
                "tata-tea-gold",
                "Tata",
                "Everyday black tea blend for morning and evening routines.",
                "Classic Indian tea blend.",
                399, null,
                "GRO-TAT-003",
                70,
                "Tea leaves",
                "Brown",
                "groceries,tea,tata,beverages",
                "Tea",
                "https://images.unsplash.com/photo-1544787219-7f47ccb76574?auto=format&fit=crop&w=1200&q=90"
        );
    }

    private void seedFashion(ProductRepository products, Category category) {

        product(
                products, category,
                "Uniqlo Supima T-Shirt",
                "uniqlo-supima-tshirt",
                "Uniqlo",
                "Soft cotton everyday T-shirt with a clean silhouette.",
                "Minimal everyday cotton T-shirt.",
                1499, 1299,
                "FAS-UNQ-001",
                32,
                "Cotton",
                "Black",
                "fashion,t-shirts,uniqlo,cotton",
                "T-Shirts",
                "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Levi's 501 Original",
                "levis-501-original",
                "Levi's",
                "Classic straight-leg denim jeans with an everyday fit.",
                "Iconic straight-fit denim.",
                4999, 4299,
                "FAS-LEV-002",
                25,
                "Denim",
                "Blue",
                "fashion,jeans,levis,denim",
                "Jeans",
                "https://images.unsplash.com/photo-1542272604-787c3835535d?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Adidas Everyday Hoodie",
                "adidas-everyday-hoodie",
                "Adidas",
                "Comfortable fleece hoodie for everyday casual wear.",
                "Everyday casual hoodie.",
                3999, 3299,
                "FAS-ADI-003",
                24,
                "Cotton fleece",
                "Grey",
                "fashion,hoodies,adidas,casual",
                "Hoodies",
                "https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=1200&q=90"
        );
    }

    private void seedBeauty(ProductRepository products, Category category) {

        product(
                products, category,
                "The Ordinary Niacinamide 10%",
                "the-ordinary-niacinamide-10",
                "The Ordinary",
                "Daily skincare serum formulated for a balanced routine.",
                "Everyday niacinamide serum.",
                799, 699,
                "BEA-ORD-001",
                30,
                "Water-based",
                "Clear",
                "beauty,serums,skincare,the-ordinary",
                "Serums",
                "https://images.unsplash.com/photo-1571781926291-c477ebfd024b?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "CeraVe Facial Cleanser",
                "cerave-facial-cleanser",
                "CeraVe",
                "Gentle facial cleanser designed for everyday skincare.",
                "Gentle daily face cleanser.",
                1199, 999,
                "BEA-CER-002",
                28,
                "Cream",
                "White",
                "beauty,face-wash,cerave,skincare",
                "Face Wash",
                "https://images.unsplash.com/photo-1598440947619-2c35fc9aa908?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Nivea Body Lotion",
                "nivea-body-lotion",
                "Nivea",
                "Moisturizing body lotion for everyday skin care.",
                "Everyday body moisturizer.",
                499, null,
                "BEA-NIV-003",
                40,
                "Lotion",
                "White",
                "beauty,body-care,nivea,moisturizer",
                "Bath & Body",
                "https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=1200&q=90"
        );
    }

    private void seedSports(ProductRepository products, Category category) {

        product(
                products, category,
                "Nike Pegasus",
                "nike-pegasus",
                "Nike",
                "Daily trainer with balanced cushioning for road running.",
                "Reliable everyday running shoe.",
                9999, 8499,
                "SPO-NIK-001",
                22,
                "Mesh",
                "Black",
                "sports-fitness,running-shoes,nike,road-running",
                "Running Shoes",
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Adidas Ultraboost",
                "adidas-ultraboost",
                "Adidas",
                "Responsive running shoe for daily miles and training.",
                "Cushioned Adidas running shoe.",
                13999, 11999,
                "SPO-ADI-002",
                18,
                "Primeknit",
                "White",
                "sports-fitness,running-shoes,adidas,training",
                "Running Shoes",
                "https://images.unsplash.com/photo-1552674605-db6ffd4facb5?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Decathlon Yoga Mat",
                "decathlon-yoga-mat",
                "Decathlon",
                "Non-slip yoga mat for stretching, mobility, and daily practice.",
                "Affordable non-slip yoga mat.",
                1299, 1099,
                "SPO-DEC-003",
                35,
                "Natural rubber",
                "Sage",
                "sports-fitness,yoga,decathlon,exercise",
                "Yoga Mats",
                "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?auto=format&fit=crop&w=1200&q=90"
        );
    }

    private void seedTravel(ProductRepository products, Category category) {

        product(
                products, category,
                "Osprey Daylite Backpack",
                "osprey-daylite-backpack",
                "Osprey",
                "Compact everyday backpack for commuting and short trips.",
                "Everyday travel backpack.",
                6999, 5999,
                "TRV-OSP-001",
                18,
                "Recycled nylon",
                "Black",
                "travel,backpacks,osprey,commute",
                "Backpacks",
                "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Samsonite Cabin Case",
                "samsonite-cabin-case",
                "Samsonite",
                "Compact cabin luggage with durable shell construction.",
                "Cabin-size travel suitcase.",
                11999, 9999,
                "TRV-SAM-002",
                15,
                "Polycarbonate",
                "Black",
                "travel,suitcases,samsonite,luggage",
                "Suitcases",
                "https://images.unsplash.com/photo-1565026057447-bc90a52d39d9?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Eagle Creek Packing Cubes",
                "eagle-creek-packing-cubes",
                "Eagle Creek",
                "Lightweight packing cubes for organized luggage.",
                "Travel packing organizers.",
                2499, 2199,
                "TRV-EAG-003",
                26,
                "Nylon",
                "Blue",
                "travel,organizers,packing,eagle-creek",
                "Travel Organizers",
                "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?auto=format&fit=crop&w=1200&q=90"
        );
    }

    private void seedFurniture(ProductRepository products, Category category) {

        product(
                products, category,
                "IKEA Bekant Desk",
                "ikea-bekant-desk",
                "IKEA",
                "Minimal workstation desk designed for productive home offices.",
                "Clean-lined work desk.",
                12999, 10999,
                "FUR-IKE-001",
                9,
                "Wood",
                "Natural",
                "furniture-decor,desks,ikea,workspace",
                "Desks",
                "https://images.unsplash.com/photo-1518455027359-f3f8164ba6b0?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Herman Miller Office Chair",
                "herman-miller-office-chair",
                "Herman Miller",
                "Ergonomic office chair designed for long working sessions.",
                "Premium ergonomic office chair.",
                89999, 79999,
                "FUR-HM-002",
                5,
                "Mesh",
                "Black",
                "furniture-decor,chairs,office,ergonomic",
                "Chairs",
                "https://images.unsplash.com/photo-1580480055273-228ff5388ef8?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "IKEA Wall Shelf",
                "ikea-wall-shelf",
                "IKEA",
                "Minimal wall shelf for practical home storage and display.",
                "Simple storage shelf.",
                2499, 2099,
                "FUR-IKE-003",
                20,
                "Wood",
                "Oak",
                "furniture-decor,shelves,ikea,storage",
                "Shelves",
                "https://images.unsplash.com/photo-1594620302200-9a762244a156?auto=format&fit=crop&w=1200&q=90"
        );
    }

    private void seedGaming(ProductRepository products, Category category) {

        product(
                products, category,
                "Razer BlackWidow V3",
                "razer-blackwidow-v3-gaming",
                "Razer",
                "Mechanical gaming keyboard with tactile switches and gaming features.",
                "Mechanical gaming keyboard.",
                13999, 11999,
                "GAM-RAZ-001",
                14,
                "Aluminium",
                "Black",
                "gaming,gaming-keyboards,razer,mechanical",
                "Gaming Keyboards",
                "https://images.unsplash.com/photo-1541140532154-b024d705b90a?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Logitech G502 Hero",
                "logitech-g502-hero",
                "Logitech",
                "High-performance gaming mouse with adjustable controls.",
                "Popular Logitech gaming mouse.",
                6999, 5999,
                "GAM-LOG-002",
                18,
                "Plastic",
                "Black",
                "gaming,gaming-mice,logitech",
                "Gaming Mice",
                "https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "HyperX Cloud III",
                "hyperx-cloud-iii",
                "HyperX",
                "Comfortable gaming headset designed for long sessions.",
                "Comfort-focused gaming headset.",
                10999, 8999,
                "GAM-HYP-003",
                16,
                "Plastic",
                "Black",
                "gaming,headsets,hyperx,audio",
                "Gaming Headsets",
                "https://images.unsplash.com/photo-1599669454699-248893623440?auto=format&fit=crop&w=1200&q=90"
        );
    }

    private void seedBooks(ProductRepository products, Category category) {

        product(
                products, category,
                "Atomic Habits",
                "atomic-habits",
                "Penguin Random House",
                "Popular practical book on building better daily habits.",
                "Practical guide to better habits.",
                699, 599,
                "BOK-PEN-001",
                40,
                "Paper",
                "White",
                "books-stationery,books,non-fiction,habits",
                "Non-Fiction Books",
                "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Leuchtturm1917 Notebook",
                "leuchtturm1917-notebook-2",
                "Leuchtturm1917",
                "Premium hardcover notebook for everyday writing and planning.",
                "Premium notebook.",
                1999, 1699,
                "BOK-LEU-002",
                35,
                "Paper",
                "Black",
                "books-stationery,notebooks,journals,writing",
                "Notebooks",
                "https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Pilot G2 Pens",
                "pilot-g2-pens",
                "Pilot",
                "Smooth-writing gel pens for study, work, and notes.",
                "Reliable everyday writing pens.",
                499, null,
                "BOK-PIL-003",
                70,
                "Plastic",
                "Black",
                "books-stationery,pens,pilot,writing",
                "Pens",
                "https://images.unsplash.com/photo-1455390582262-044cdead277a?auto=format&fit=crop&w=1200&q=90"
        );
    }

    private void seedAutomotive(ProductRepository products, Category category) {

        product(
                products, category,
                "Spigen Car Phone Mount",
                "spigen-car-phone-mount-2",
                "Spigen",
                "Adjustable vehicle phone mount for everyday driving.",
                "Secure car phone holder.",
                2299, 1899,
                "AUT-SPI-001",
                26,
                "ABS plastic",
                "Black",
                "automotive,phone-mount,spigen,car",
                "Car Phone Mounts",
                "https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "70mai Dash Cam",
                "70mai-dash-cam",
                "70mai",
                "Compact dash camera for recording everyday journeys.",
                "Compact car dash camera.",
                5999, 5299,
                "AUT-70M-002",
                14,
                "Plastic",
                "Black",
                "automotive,dash-cameras,70mai,car",
                "Dash Cameras",
                "https://images.unsplash.com/photo-1502877338535-766e1452684a?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Meguiar's Car Cleaning Kit",
                "meguiars-car-cleaning-kit",
                "Meguiar's",
                "Complete car cleaning kit for regular vehicle maintenance.",
                "Everyday car care kit.",
                2999, 2499,
                "AUT-MEG-003",
                21,
                "Liquid",
                "Blue",
                "automotive,cleaning-kits,meguiars,car-care",
                "Cleaning Kits",
                "https://images.unsplash.com/photo-1607860108855-64acf2078ed9?auto=format&fit=crop&w=1200&q=90"
        );
    }

    private void seedPets(ProductRepository products, Category category) {

        product(
                products, category,
                "Royal Canin Adult Dog Food",
                "royal-canin-adult-dog-food",
                "Royal Canin",
                "Balanced everyday dog food designed for adult dogs.",
                "Complete adult dog food.",
                1899, 1699,
                "PET-ROY-001",
                28,
                "Dry food",
                "Brown",
                "pet-supplies,dog-food,royal-canin,dogs",
                "Dog Food",
                "https://images.unsplash.com/photo-1560807707-8cc77767d783?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "Purina Cat Food",
                "purina-cat-food",
                "Purina",
                "Everyday cat food formulated for adult cats.",
                "Complete adult cat food.",
                899, 799,
                "PET-PUR-002",
                30,
                "Dry food",
                "Brown",
                "pet-supplies,cat-food,purina,cats",
                "Cat Food",
                "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?auto=format&fit=crop&w=1200&q=90"
        );

        product(
                products, category,
                "KONG Classic Dog Toy",
                "kong-classic-dog-toy",
                "KONG",
                "Durable enrichment toy designed for playful dogs.",
                "Classic interactive dog toy.",
                999, 899,
                "PET-KON-003",
                38,
                "Rubber",
                "Red",
                "pet-supplies,pet-toys,kong,dogs",
                "Pet Toys",
                "https://images.unsplash.com/photo-1583337130417-3346a1be7dee?auto=format&fit=crop&w=1200&q=90"
        );
    }

        // -------------------------------------------------------------------------
        // COMPLETE TYPE COVERAGE
        // -------------------------------------------------------------------------

        private void seedCompleteTypeCoverage(ProductRepository products, Map<String, Category> categories) {
                Map<String, String[]> types = Map.ofEntries(
                                Map.entry("electronics", new String[]{"Headphones", "Earbuds", "Smartphones", "Laptops", "Keyboards", "Mice", "Speakers", "Cameras", "Monitors", "Smartwatches"}),
                                Map.entry("home-kitchen", new String[]{"Cookware", "Coffee Makers", "Mixers & Blenders", "Air Fryers", "Kitchen Tools", "Dinnerware", "Storage & Organization", "Lighting", "Home Appliances", "Water Bottles"}),
                                Map.entry("groceries", new String[]{"Rice & Grains", "Pulses", "Snacks", "Cereals", "Coffee", "Tea", "Spices", "Cooking Oil", "Beverages", "Dry Fruits"}),
                                Map.entry("fashion", new String[]{"T-Shirts", "Shirts", "Jeans", "Trousers", "Jackets", "Hoodies", "Dresses", "Shoes", "Sandals", "Bags"}),
                                Map.entry("beauty", new String[]{"Face Wash", "Moisturizers", "Serums", "Shampoo", "Hair Care", "Body Lotion", "Makeup", "Lip Care", "Grooming Kits", "Bath & Body"}),
                                Map.entry("sports-fitness", new String[]{"Running Shoes", "Sportswear", "Yoga Mats", "Dumbbells", "Resistance Bands", "Gym Bags", "Fitness Trackers", "Water Bottles", "Sports Accessories", "Training Equipment"}),
                                Map.entry("travel", new String[]{"Backpacks", "Suitcases", "Duffel Bags", "Travel Organizers", "Passport Holders", "Neck Pillows", "Travel Bottles", "Packing Cubes", "Laptop Bags", "Travel Accessories"}),
                                Map.entry("furniture-decor", new String[]{"Desks", "Chairs", "Tables", "Lamps", "Rugs", "Cushions", "Shelves", "Wall Decor", "Storage Units", "Mirrors"}),
                                Map.entry("gaming", new String[]{"Gaming Keyboards", "Gaming Mice", "Controllers", "Gaming Headsets", "Webcams", "Mouse Pads", "Gaming Chairs", "Gaming Monitors", "Console Accessories", "Streaming Accessories"}),
                                Map.entry("books-stationery", new String[]{"Fiction Books", "Non-Fiction Books", "Textbooks", "Notebooks", "Journals", "Pens", "Pencils", "Art Supplies", "Planners", "Study Materials"}),
                                Map.entry("automotive", new String[]{"Car Phone Mounts", "Car Chargers", "Seat Covers", "Cleaning Kits", "Air Fresheners", "Car Organizers", "Dash Cameras", "Floor Mats", "Car Tools", "Automotive Accessories"}),
                                Map.entry("pet-supplies", new String[]{"Dog Food", "Cat Food", "Pet Toys", "Collars", "Leashes", "Pet Beds", "Grooming Supplies", "Feeding Bowls", "Pet Treats", "Pet Accessories"})
                );
                int index = 1;
                for (Map.Entry<String, String[]> entry : types.entrySet()) {
                        Category category = categories.get(entry.getKey());
                        for (String type : entry.getValue()) {
                                String[] brands = brandsFor(entry.getKey(), type);
                                List<String> images = previewImages(entry.getKey(), type);
                                for (int variant = 0; variant < 3; variant++) {
                                        String brand = brands[variant];
                                        String model = modelFor(type, variant);
                                        String slug = (entry.getKey() + "-" + type + "-" + brand + "-" + variant).toLowerCase().replaceAll("[^a-z0-9]+", "-");
                                        product(products, category, brand + " " + model, slug, brand,
                                                        "A reliable " + type.toLowerCase() + " from " + brand + " for everyday use.",
                                                            brand + " " + type + " for everyday use.", priceFor(entry.getKey(), type, index), discountFor(entry.getKey(), type, index, variant),
                                                        "CAT-" + entry.getKey().substring(0, 3).toUpperCase() + "-" + index + "-" + variant, 12 + variant * 4,
                                                        materialFor(entry.getKey()), colorFor(variant), entry.getKey() + "," + type.toLowerCase().replace(' ', ',') + "," + brand.toLowerCase().replace(' ', ','), type, images.get(variant));
                                }
                                index++;
                        }
                }
        }

        private String[] brandsFor(String category, String type) {
                String key = type.toLowerCase();
                if (category.equals("electronics")) {
                        if (key.contains("headphone")) return new String[]{"Sony", "Bose", "JBL"};
                        if (key.contains("earbud")) return new String[]{"Apple", "Samsung", "OnePlus"};
                        if (key.contains("smartphone")) return new String[]{"Samsung", "Google", "OnePlus"};
                        if (key.contains("laptop")) return new String[]{"Dell", "Lenovo", "ASUS"};
                        if (key.contains("camera")) return new String[]{"Canon", "Sony", "Nikon"};
                        return new String[]{"Logitech", "Razer", "JBL"};
                }
                if (category.equals("pet-supplies")) {
                        if (key.contains("dog")) return new String[]{"Royal Canin", "Purina", "Pedigree"};
                        if (key.contains("cat")) return new String[]{"Purina", "Royal Canin", "Whiskas"};
                        return new String[]{"KONG", "Purina", "PetSafe"};
                }
                if (category.equals("fashion")) return key.contains("shoe") || key.contains("sandal") ? new String[]{"Nike", "Adidas", "Puma"} : new String[]{"Uniqlo", "Levi's", "Adidas"};
                if (category.equals("sports-fitness")) return new String[]{"Nike", "Adidas", "Decathlon"};
                if (category.equals("travel")) return new String[]{"Osprey", "Samsonite", "American Tourister"};
                if (category.equals("beauty")) return new String[]{"The Ordinary", "CeraVe", "Nivea"};
                if (category.equals("groceries")) return new String[]{"Tata", "India Gate", "Dabur"};
                if (category.equals("home-kitchen")) return new String[]{"Philips", "Prestige", "Havells"};
                if (category.equals("furniture-decor")) return new String[]{"IKEA", "Urban Ladder", "Wakefit"};
                if (category.equals("gaming")) return new String[]{"Razer", "Logitech", "Corsair"};
                if (category.equals("books-stationery")) return new String[]{"Penguin", "Pilot", "Classmate"};
                return new String[]{"Bosch", "3M", "Philips"};
        }

        private List<String> previewImages(String category, String type) {
                String token = (category + "-" + type).toLowerCase();
                if (token.contains("headphone") || token.contains("headset")) return List.of("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=1200&q=90", "https://images.unsplash.com/photo-1484704849700-f032a568e944?auto=format&fit=crop&w=1200&q=90", "https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=1200&q=90");
                if (token.contains("earbud")) return List.of("https://images.unsplash.com/photo-1606220945770-b5b6c2c55bf1?auto=format&fit=crop&w=1200&q=90", "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?auto=format&fit=crop&w=1200&q=90", "https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?auto=format&fit=crop&w=1200&q=90");
                if (token.contains("laptop")) return List.of("https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=1200&q=90", "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=1200&q=90", "https://images.unsplash.com/photo-1531297484001-80022131f5a1?auto=format&fit=crop&w=1200&q=90");
                if (token.contains("pet") || category.equals("pet-supplies")) return List.of("https://images.unsplash.com/photo-1560807707-8cc77767d783?auto=format&fit=crop&w=1200&q=90", "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?auto=format&fit=crop&w=1200&q=90", "https://images.unsplash.com/photo-1583337130417-3346a1be7dee?auto=format&fit=crop&w=1200&q=90");
                if (category.equals("fashion")) return List.of("https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=1200&q=90", "https://images.unsplash.com/photo-1542272604-787c3835535d?auto=format&fit=crop&w=1200&q=90", "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1200&q=90");
                if (category.equals("groceries")) return List.of("https://images.unsplash.com/photo-1536304993881-ff6e9eefa2a6?auto=format&fit=crop&w=1200&q=90", "https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=1200&q=90", "https://images.unsplash.com/photo-1544787219-7f47ccb76574?auto=format&fit=crop&w=1200&q=90");
                return List.of("https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=1200&q=90", "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=1200&q=90", "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=1200&q=90");
        }

        private String modelFor(String type, int variant) { return type + " Series " + (variant + 1); }
        private int priceFor(String category, String type, int index) { return category.equals("gaming") && type.equals("Gaming Mice") ? 2499 + (index % 3) * 500 : 899 + ((index * 317) % 18000); }
        private Integer discountFor(String category, String type, int index, int variant) { return variant == 0 ? priceFor(category, type, index) - 100 : null; }
        private String materialFor(String category) { return category.equals("fashion") ? "Cotton" : category.equals("groceries") ? "Food-grade" : "Recycled materials"; }
        private String colorFor(int variant) { return new String[]{"Black", "White", "Graphite"}[variant]; }

        // -------------------------------------------------------------------------
        // PRODUCT HELPER
        // -------------------------------------------------------------------------

    private void product(
            ProductRepository repository,
            Category category,
            String name,
            String slug,
            String brand,
            String description,
            String shortDescription,
            int price,
            Integer discount,
            String sku,
            int stock,
            String material,
            String color,
            String tags,
            String productType,
            String image
    ) {
        Product product = repository.findBySku(sku).orElseGet(Product::new);

        product.setCategory(category);
        product.setProductType(productType);
        product.setName(name);
        product.setSlug(slug);
        product.setBrand(brand);
        product.setDescription(description);
        product.setShortDescription(shortDescription);
        product.setPrice(BigDecimal.valueOf(price));
        product.setDiscountPrice(
                discount == null
                        ? null
                        : BigDecimal.valueOf(discount)
        );
        product.setSku(sku);
        product.setStock(stock);
        product.setMaterial(material);
        product.setColor(color);
        product.setTags(tags);
        product.setImageUrl(image);

        /*
         * Deterministic demo ratings.
         * Keep values within 3.8–4.8.
         */
        double rating = 3.8 + ((Math.abs(sku.hashCode()) % 11) / 10.0);

        product.setRating(
                BigDecimal.valueOf(Math.min(4.8, rating))
        );

        product.setReviewCount(
                12 + Math.abs(sku.hashCode() % 180)
        );

        repository.save(product);
    }
}