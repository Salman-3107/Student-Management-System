package com.example.student_management_system;

import java.util.List;

/**
 * Centralised static factory for all department data.
 * Used by both HomepageController and DepartmentsPageController.
 */
public class DeptData {

    public static DepartmentInfo CE() {
        return new DepartmentInfo(
            "Civil Engineering", "CE",
            "One of the founding departments of BUET, focusing on structural, environmental, geotechnical, and transportation engineering.",
            List.of("/com/example/student_management_system/ce1.jpg",
                    "/com/example/student_management_system/ce2.jpg"),
            List.of(
                "The Department of Civil Engineering is one of the oldest and most prestigious departments at BUET. Established at the very founding of the university, it has shaped generations of civil engineers who have contributed to Bangladesh's infrastructure development.",
                "The department offers Bachelor's, Master's, and Ph.D. degrees and covers a broad range of sub-disciplines including structural engineering, geotechnical engineering, transportation engineering, environmental engineering, water resources, and construction management.",
                "Research in the department addresses critical national challenges such as flood control, earthquake resilience, urban traffic management, and sustainable construction. Faculty members regularly publish in top international journals and collaborate with global institutions.",
                "Graduates of the CE department are employed in leading construction companies, government agencies, and international organizations worldwide, and many have achieved distinguished positions in academia and industry."
            )
        );
    }

    public static DepartmentInfo EEE() {
        return new DepartmentInfo(
            "Electrical and Electronic Engineering", "EEE",
            "Covering power systems, electronics, communication, control systems, and signal processing.",
            List.of("/com/example/student_management_system/eee1.webp"),
            List.of(
                "The Department of Electrical and Electronic Engineering (EEE) is one of the largest and most active departments at BUET. It has produced thousands of engineers who now lead Bangladesh's power sector, telecommunications industry, and technology companies.",
                "The undergraduate and graduate programs cover a wide range of topics including electric power generation and distribution, microelectronics, communication systems, digital signal processing, control engineering, and embedded systems.",
                "The department has state-of-the-art laboratories for power electronics, high-voltage engineering, telecommunications, VLSI design, and robotics. Faculty members are engaged in funded research on renewable energy, smart grids, and IoT.",
                "EEE graduates are employed in DESCO, BPDB, Grameenphone, Robi, Huawei, Samsung, and many multinational technology companies. Many alumni hold senior engineering and management positions globally."
            )
        );
    }

    public static DepartmentInfo CSE() {
        return new DepartmentInfo(
            "Computer Science and Engineering", "CSE",
            "Pioneer of CS education in Bangladesh since 1984, ranked 301–350 globally in QS Rankings by Subject.",
            List.of("/com/example/student_management_system/cse1.jpg"),
            List.of(
                "The Department of Computer Science and Engineering (CSE) at BUET, established in 1984, is the pioneer of computer science and engineering education in Bangladesh. As the first department of its kind in the country, CSE BUET has consistently set the standard for excellence in education, research, and innovation.",
                "The department offers Bachelor's, Master's, and Ph.D. degrees in Computer Science and Engineering, and regularly updates the curriculum to maintain international standards. It also offers specialised postgraduate programs in AI & ML, Data Science, Cyber Security, Software Engineering, and Computing.",
                "Currently, the department hosts about 700 undergraduate students and 400 graduate students. Students have participated in the ICPC world finals regularly for three decades, and became Asia West Champions in 2021 and 2023.",
                "Faculty members have received prestigious awards such as Bangladesh Academy of Sciences (BAS) Gold Medals, OWSD-Elsevier Award, and UGC Gold Medals. Alumni serve as professors at the University of Michigan, Columbia University, Pennsylvania State University, and University of Toronto.",
                "The department is equipped with state-of-the-art laboratories including the IoT Lab, AI & Robotics Lab, Machine Learning Lab, Samsung Applied Machine Learning Lab, VLSI Lab, and Wireless Network Lab."
            )
        );
    }

    public static DepartmentInfo ME() {
        return new DepartmentInfo(
            "Mechanical Engineering", "ME",
            "Covering thermodynamics, fluid mechanics, manufacturing, machine design, robotics, and energy systems.",
            List.of("/com/example/student_management_system/me1.jpg"),
            List.of(
                "The Department of Mechanical Engineering at BUET is one of the core engineering departments, with a long history of producing skilled mechanical engineers who drive industrial development in Bangladesh and abroad.",
                "The curriculum spans thermodynamics, fluid mechanics, heat transfer, machine design, manufacturing processes, robotics, and energy systems. Students work in well-equipped laboratories covering CAD/CAM, materials testing, thermodynamics, and advanced manufacturing.",
                "Research areas include renewable energy systems, computational fluid dynamics, industrial automation, and sustainable manufacturing. The department collaborates with industries and government agencies to solve engineering challenges in energy, transportation, and production.",
                "ME graduates are employed in power plants, manufacturing industries, oil and gas companies, automobile sectors, and government engineering services. Many also pursue advanced degrees at top international universities."
            )
        );
    }

    public static DepartmentInfo ChE() {
        return new DepartmentInfo(
            "Chemical Engineering", "ChE",
            "Working on process design, reaction engineering, industrial chemistry, and energy systems.",
            List.of("/com/example/student_management_system/che1.webp"),
            List.of(
                "The Department of Chemical Engineering at BUET prepares engineers to design, operate, and optimise chemical and industrial processes. It plays a key role in supporting Bangladesh's pharmaceutical, textile, food processing, and energy sectors.",
                "Core areas include chemical reaction engineering, thermodynamics, mass and heat transfer, process control, polymer engineering, and environmental engineering. Students gain hands-on experience in well-equipped process labs.",
                "Research themes include clean energy technologies, wastewater treatment, drug delivery systems, and bio-based chemical production. The department collaborates with industries and regulatory bodies on safety and environmental compliance.",
                "Graduates pursue careers in pharmaceutical companies, fertilizer plants, petroleum refineries, food processing industries, and environmental engineering firms, both domestically and internationally."
            )
        );
    }

    public static DepartmentInfo IPE() {
        return new DepartmentInfo(
            "Industrial and Production Engineering", "IPE",
            "Focusing on operations research, manufacturing systems, quality control, and supply chain management.",
            List.of("/com/example/student_management_system/ipe1.jpg"),
            List.of(
                "The Department of Industrial and Production Engineering (IPE) at BUET trains engineers to optimise industrial systems, improve productivity, and manage complex manufacturing and service operations.",
                "Core subjects include operations research, production planning, ergonomics, quality control, supply chain management, simulation, and lean manufacturing. The department bridges the gap between engineering and management.",
                "Research focuses on industrial efficiency, garment and textile manufacturing optimisation (critical for Bangladesh's export economy), healthcare systems management, and smart factory concepts under Industry 4.0.",
                "Graduates work in the garment industry, manufacturing companies, logistics firms, banks, and consulting organisations. IPE graduates are highly valued for their analytical and management skills in both technical and business environments."
            )
        );
    }

    public static DepartmentInfo WRE() {
        return new DepartmentInfo(
            "Water Resources Engineering", "WRE",
            "Dedicated to hydrology, flood control, irrigation, river engineering, and sustainable water management.",
            List.of("/com/example/student_management_system/wre1.webp"),
            List.of(
                "The Department of Water Resources Engineering at BUET is uniquely positioned to address Bangladesh's most critical environmental challenge — water. Given the country's riverine geography and vulnerability to flooding and drought, this department plays a vital national role.",
                "The department offers programs focusing on hydrology, groundwater, fluvial hydraulics, coastal engineering, irrigation systems, and climate adaptation strategies. Students learn to design solutions for flood mitigation, drainage, and water supply.",
                "Faculty members work closely with government bodies such as BWDB (Bangladesh Water Development Board) and international agencies to develop evidence-based policy and engineering solutions for water management.",
                "Graduates pursue careers in hydraulic engineering, environmental consulting, government planning departments, and international development organisations such as the World Bank and UN agencies."
            )
        );
    }

    public static DepartmentInfo NAME() {
        return new DepartmentInfo(
            "Naval Architecture and Marine Engineering", "NAME",
            "Studying ship design, marine structures, offshore engineering, and maritime technology.",
            List.of("/com/example/student_management_system/name1.jpg"),
            List.of(
                "The Department of Naval Architecture and Marine Engineering (NAME) at BUET is the only department of its kind in Bangladesh, making it strategically vital for a country with extensive inland waterways and a growing maritime economy.",
                "The program covers ship design, hull structures, marine propulsion, offshore platforms, vessel stability, hydrodynamics, and maritime safety. Students work with industry partners including shipyards and shipping companies.",
                "Bangladesh has one of the world's most active shipbuilding industries. Research in the department directly supports this sector through work on efficient hull design, vessel performance optimisation, and sustainable maritime transport.",
                "Graduates are employed in shipbuilding yards, the Bangladesh Inland Water Transport Authority (BIWTA), offshore energy companies, maritime consulting firms, and the Bangladesh Navy."
            )
        );
    }

    public static DepartmentInfo BME() {
        return new DepartmentInfo(
            "Biomedical Engineering", "BME",
            "Combining engineering and medical science for healthcare devices, diagnostics, and rehabilitation technologies.",
            List.of("/com/example/student_management_system/bme1.webp"),
            List.of(
                "The Department of Biomedical Engineering (BME) at BUET is one of the newer and rapidly growing departments, reflecting the global shift toward healthcare technology. It bridges the gap between engineering principles and medical science to develop solutions for human health.",
                "The program covers biomedical instrumentation, medical imaging, biomechanics, rehabilitation engineering, biosignal processing, and clinical engineering. Students gain exposure to real-world healthcare problems through collaborations with hospitals and medical institutions.",
                "Research in the department addresses pressing healthcare challenges in Bangladesh and beyond, including affordable diagnostic devices, prosthetics, and telemedicine systems. Faculty members collaborate with international biomedical research centres.",
                "Graduates pursue careers in medical device companies, hospitals, research institutions, and healthcare startups, both in Bangladesh and internationally."
            )
        );
    }

    public static DepartmentInfo MME() {
        return new DepartmentInfo(
            "Materials and Metallurgical Engineering", "MME",
            "Dealing with metals, alloys, polymers, ceramics, corrosion, and materials characterization.",
            List.of("/com/example/student_management_system/mme1.jpg"),
            List.of(
                "The Department of Materials and Metallurgical Engineering (MME) at BUET focuses on the science and engineering of materials — from metals and alloys to polymers and composites — that underpin all modern technology.",
                "The curriculum covers physical metallurgy, materials characterisation, corrosion engineering, polymer science, semiconductor materials, and manufacturing processes. State-of-the-art labs support electron microscopy, X-ray diffraction, and mechanical testing.",
                "Research addresses materials for energy applications, corrosion prevention in infrastructure, high-performance alloys for aerospace and defence, and biomaterials for medical devices. Faculty members publish widely in materials science journals.",
                "Graduates work in steel mills, electronics manufacturing, automotive industries, defence organisations, and materials research institutes. Many alumni pursue postgraduate studies at leading global universities."
            )
        );
    }

    public static DepartmentInfo NCE() {
        return new DepartmentInfo(
            "Nanomaterials and Ceramic Engineering", "NCE",
            "Focusing on advanced ceramics, nanotechnology, functional materials, and composites.",
            List.of("/com/example/student_management_system/nce1.jpg"),
            List.of(
                "The Department of Nanomaterials and Ceramic Engineering (NCE) at BUET is a forward-looking department addressing the growing demand for advanced functional materials in electronics, energy, and healthcare.",
                "Programs cover ceramic processing, nanomaterial synthesis, thin film technology, semiconductor devices, piezoelectrics, and biomaterials. Students work in specialised nanofabrication and materials characterisation facilities.",
                "Research explores energy harvesting materials, photocatalysts for water purification, bioceramics for implants, and nanocomposites for structural applications. The department contributes to Bangladesh's ambition to develop a knowledge-based technology economy.",
                "Graduates find opportunities in electronics manufacturing, renewable energy companies, defence research, and materials technology startups, as well as in advanced postgraduate programs worldwide."
            )
        );
    }

    public static DepartmentInfo Arch() {
        return new DepartmentInfo(
            "Architecture", "Arch",
            "Focusing on building design, urban form, sustainability, aesthetics, and human-centered spaces.",
            List.of("/com/example/student_management_system/arch1.webp"),
            List.of(
                "The Department of Architecture at BUET is the premier architecture school in Bangladesh, producing designers who shape the built environment of the country. The department nurtures creativity, technical skill, and social responsibility in equal measure.",
                "The five-year professional Bachelor of Architecture program covers design studios, building construction, architectural history and theory, environmental systems, urban design, and professional practice. Students complete real-world projects and internships.",
                "Research and practice in the department address tropical architecture, climate-responsive design, heritage conservation, affordable housing, and sustainable urban development — issues deeply relevant to Bangladesh's rapid urbanisation.",
                "Graduates of BUET Architecture lead Bangladesh's top architectural firms, work with international design organisations, and hold academic positions at universities worldwide. Many have won prestigious national and international design awards."
            )
        );
    }

    public static DepartmentInfo URP() {
        return new DepartmentInfo(
            "Urban and Regional Planning", "URP",
            "Studying city planning, land use, transportation systems, housing, and regional development.",
            List.of("/com/example/student_management_system/urp1.jpg"),
            List.of(
                "The Department of Urban and Regional Planning (URP) at BUET addresses one of Bangladesh's most pressing challenges — how to plan, manage, and sustain rapidly growing cities and regions in a context of dense population and climate vulnerability.",
                "The curriculum covers land use planning, transportation planning, GIS and remote sensing, housing policy, environmental planning, and development economics. Students undertake studio projects in real Bangladeshi cities and districts.",
                "Research in the department focuses on Dhaka's urban growth management, flood-resilient city planning, slum upgrading, and regional development strategies. Faculty members advise government agencies including RAJUK and the Planning Commission.",
                "Graduates work in city corporations, government planning departments, international development agencies (UNDP, UN-Habitat, World Bank), NGOs, and consulting firms. URP professionals are essential to Bangladesh's sustainable development goals."
            )
        );
    }
}
