CREATE (bank:Package { fqn : 'bank', name: 'bank', hash: 'ID_bdd240c8fe7174e6ac1cfdd5282de76eb7ad6815'})
CREATE (p:Package { fqn : 'bank.products', name: 'products', hash: 'ID_052028080c3d64381f56397d59113102bf4e5921' })
CREATE (c:Package { fqn : 'bank.customer', name: 'customer', hash: 'ID_4481fcdc97864a546f67c76536e0308a3058f75d' })
CREATE (bc:Class:Type { fqn : 'bank.customer.BusinessCustomer', name: 'BusinessCustomer', hash: 'ID_26f25e4da4c82dc2370f3bde0201e612dd88c04c'})
CREATE (pc:Class:Type { fqn : 'bank.customer.PrivateCustomer', name: 'PrivateCustomer', hash: 'ID_9fa088272bab165867bfaddcfbf58d6a7d5d45a2' })
CREATE (cd:Class:Type { fqn : 'bank.products.Credit', name : 'Credit', hash: 'ID_fba940eea4ca7f9002bc529bbb0cbc8fc0423985' })
CREATE (ac:Class:Type { fqn : 'bank.products.Account', name : 'Account', hash: 'ID_e14c10820a526fde0ea0beee073b947e1ca67a4a' })
CREATE (ab:Class:Type { fqn : 'bank.products.AbstractProduct', name : 'AbstractProduct', hash: 'ID_ecbec7f0190fc1c903a87797d35427dc7a1f240b' })
CREATE (tr:Class:Type { fqn : 'bank.products.Transaction', name : 'Transaction', hash: 'ID_841936e6f873f49a85e18a4a94df5d7dfe591edf' })
CREATE (bb:Class:Type { fqn : 'bank.products.BankBook', name : 'BankBook', hash: 'ID_9e346826c780b8b1975dc8906296cc47974ccd4e' })
CREATE (bk:Class:Type { fqn : 'bank.Bank', name : 'Bank', hash : 'ID_527aa1c76ab5cca95e6dbfcea35a5d2d9f5d737f' })
CREATE (m1:Method { fqn : 'bank.customer.BusinessCustomer.setName(java.lang.String)', name: 'setName', hash: 'ID_d2955e64b6776b754f9d69f8f480a62f849584ca', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m2:Method { fqn : 'bank.customer.BusinessCustomer.getName(java.lang.String)', name: 'getName', hash: 'ID_8e6fd4f517317d9e25b423eb141bad20f6e0fe62', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m3:Method { fqn : 'bank.customer.BusinessCustomer.getAccounts()', name: 'getAccounts', hash: ' D_10765258b141addc153f07acfb7ed26b00fe22fb', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m4:Method { fqn : 'bank.customer.BusinessCustomer.getCredits()', name: 'getCredits', hash: 'ID_2ff793ad7485749918f01f511c989ceec094cfc5', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m5:Method { fqn : 'bank.customer.PrivateCustomer.getBankBook()', name: 'getBankBook', hash: 'ID_ebace4173ecbaaa72dafda3332424aa4a233d1fc', cyclomaticComplexity: 1, effectiveLineCount: 70, visibility: 'public' })
CREATE (m6:Method { fqn : 'bank.customer.PrivateCustomer.getAccounts()', name: 'getAccounts', hash: 'ID_01de56af912219c3217ee6d97c38f3731fb4052c', cyclomaticComplexity: 1, effectiveLineCount: 70, visibility: 'public' })
CREATE (m7:Method { fqn : 'bank.customer.PrivateCustomer.credits()', name: 'credits', hash: 'ID_ca33f0111d29b1cc3827500605a2db32311317ce', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m8:Method { fqn : 'bank.customer.PrivateCustomer.setName(java.lang.String)', name: 'setName',hash: 'ID_a9a2c0261fbc4981279fc288ed89617ba4f5acf8', cyclomaticComplexity: 1, effectiveLineCount: 70, visibility: 'public' })
CREATE (m9:Method { fqn : 'bank.customer.PrivateCustomer.getName()', name: 'getName', hash: 'ID_fc94cf011272f8e738d5600f64861a4a0d2cc6d2', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m10:Method { fqn : 'bank.products.Credit.transaction(bank.products.Transaction)', name : 'transaction', hash: 'ID_17bd18bd602e8e22b6c1db22c5d32a02542ed1ed', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m11:Method { fqn : 'bank.products.Account.transaction(bank.products.Transaction)', name : 'transaction', hash: 'ID_ef646766f217c78d4a7ce6bd849dc15f9cc6f676', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m12:Method { fqn : 'bank.products.AbstractProduct.getExecutedTransactions()', name : 'getExecutedTransactions', hash: 'ID_500db1187a5b547cb7d799c03f327421766bfc2a', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m13:Method { fqn : 'bank.products.AbstractProduct.Transaction()', name : 'Transaction', hash: 'ID_a113953a4fd76dc7bd442656a08af378baed6667', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m14:Method { fqn : 'bank.products.AbstractProduct.executeTransaction(bank.products.Transaction', name : 'executeTransaction', hash: 'ID_61658aef236e200a9ffa30030e7cb0549bef0d4f', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m15:Method { fqn : 'bank.products.AbstractProduct.transaction(bank.products.Transaction)', name : 'transaction', hash : 'ID_78cd3ffe06ad51757c7facd4bbfdd3130313e03b', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m16:Method { fqn : 'bank.products.AbstractProduct.getProductNumber()', name : 'getProductNumber', hash: 'ID_ffec14a7831c9af6e99a59101779abcfbc7be97f', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m17:Method { fqn : 'bank.products.AbstractProduct.getBalance()', name : 'getBalance', hash : 'ID_c0536af5045f9030acfa718623dec662d3f00112', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m18:Method { fqn : 'bank.products.Transaction.getSecondProduct()', name : 'getSecondProduct', hash: 'ID_b855b93bf7c9e35b15e205e2fa18853a05d54af0', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m19:Method { fqn : 'bank.products.Transaction.getFirstProduct()', name : 'getFirstProduct', hash: 'ID_c01f09c13775d8d9ba1a4e431bdd87296d4e8a52', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m20:Method { fqn : 'bank.products.BankBook.transaction(bank.products.Transaction)', name : 'transaction', hash: 'ID_9ede89834e464def4f7d991a98d3117a8368f701', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m21:Method { fqn : 'bank.Bank.setBankName(java.lang.String)', name: 'setBankName', hash: 'ID_f734e3036b271f56e1dbb9ca69f4aa920b2f98c2', cyclomaticComplexity: 5, effectiveLineCount: 70, visibility: 'public' })
CREATE (m22:Method { fqn : 'bank.Bank.getBankName()', name: 'getBankName', hash: 'ID_86d6c1788619dad7ac26b6e15d350d143c7871dc', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m23:Method { fqn : 'bank.Bank.getBankBooks()', name: 'getBankBooks', hash: 'ID_a874289371d9f390c9f8b6daccf26dfb5a7ba1cf', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m24:Method { fqn : 'bank.Bank.getBusinessCustomers()', name: 'getBusinessCustomers', hash: 'ID_61c70ba0ef686b48fead0df0364453c5afb83618', cyclomaticComplexity: 40, effectiveLineCount: 70, visibility: 'public' })
CREATE (m25:Method { fqn : 'bank.Bank.getTransactions()', name: 'getTransactions', hash: 'ID_994a3789160d04dfc93a3be914f46c023d4f9d20', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m26:Method { fqn : 'bank.Bank.getAccounts()', name: 'getAccounts', hash: 'ID_e73864b238f8e1eb24957648d47fb9c528dd02ff', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (m27:Method { fqn : 'bank.Bank.getPrivateCustomers()', name: 'getPrivateCustomers', hash: 'ID_119adb8ccd123c522bc2026f3c4542a65094c714', cyclomaticComplexity: 43, effectiveLineCount: 70, visibility: 'public' })
CREATE (m28:Method { fqn : 'bank.Bank.getCredits()', name: 'getCredits', hash: 'ID_3dbf08a296ea12b3d7da059af2c7343c26780d29', cyclomaticComplexity: 12, effectiveLineCount: 70, visibility: 'public' })
CREATE (nameBusiness:Field { fqn : 'bank.customer.BusinessCustomer.name', name: 'name', hash: 'ID_11629cab6286835bcf468dd158c63816c6bc8b50', visibility: 'public' })
CREATE (accountsBusiness:Field { fqn : 'bank.customer.BusinessCustomer.accounts', name: 'accounts', hash: 'ID_4d08a19b31cdb900d36f69756e8323ca65e9186b', visibility: 'public' })
CREATE (creditsBusiness:Field { fqn : 'bank.customer.BusinessCustomer.credits', name : 'credits', hash: 'ID_60e93a9ac46923cab21c9b4ca6bd6b4fdcd2d55', visibility: 'public' })
CREATE (bankBook:Field { fqn : 'bank.customer.PrivateCustomer.bankBook', name: 'bankBook', hash: 'ID_2f90fe03451cdda1f5129e993c409ab531118027', visibility: 'public' })
CREATE (accountsPrivate:Field { fqn:  'bank.customer.PrivateCustomer.accounts', name: 'accounts', hash: 'ID_4754aad0f8ca2e5adb39c097f3221eda4c82877d', visibility: 'public' })
CREATE (creditsPrivate:Field { fqn : 'bank.customer.PrivateCustomer.credits', name: 'credits', hash : 'ID_0166fed357196a09244efe558dbeb27e6e0d646b', visibility: 'public' })
CREATE (namePrivate:Field { fqn : 'bank.customer.PrivateCustomer.name', name: 'name' , hash: 'ID_0cf4af5bed915ef9b74c93b2c50852c21f2d5364', visibility: 'public' })
CREATE (executeTransaction:Field { fqn : 'bank.products.AbstractProduct.executedTransactions', name : 'executedTransactions', hash: 'ID_c7bc3b30c85e34e537f5ada76dafa4bb3f77646b', visibility: 'public' })
CREATE (transactionIn:Field { fqn : 'bank.products.AbstractProduct.Transaction', name: 'Transaction', hash: 'ID_a113953a4fd76dc7bd442656a08af378baed6667', visibility: 'public' })
CREATE (productNumber:Field { fqn : 'bank.products.AbstractProduct.productNumber', name: 'productNumber', hash: 'ID_37834fec4533797c12daa8509eb11587547caac1', visibility: 'public' })
CREATE (balance:Field { fqn : 'bank.products.AbstractProduct.balance', name: 'balance', hash: 'ID_dce8b98f162544734c5aacebb0a3731ee104eb77', visibility: 'public' })
CREATE (secondProduct:Field { fqn : 'bank.products.Transaction.secondProduct', name: 'secondProduct', hash: 'ID_dbe90bb7628212f56ef88f4a152bc0fe402f350b', visibility: 'public' })
CREATE (firstProduct:Field { fqn : 'bank.products.Transaction.firstProduct', name: 'firstProduct', hash: 'ID_772a992774d132a9f8417a602abf7174e7ef4a94', visibility: 'public' })
CREATE (bankName:Field { fqn: 'bank.Bank.bankName', name: 'bankName', hash: 'ID_cc266d70c84afda695fa63dd8acbee9624f2092a', visibility: 'public' })
CREATE (bankBooks: Field { fqn : 'bank.Bank.bankBooks', name: 'bankBooks', hash: 'ID_4ece969a895e6ec11e96612d2a45b1a0acc47942', visibility: 'public' })
CREATE (businessCustomers:Field { fqn: 'bank.Bank.businessCustomers', name: 'businessCustomers', hash: 'ID_e511c29ffb94450ed119cc73fc706d047e45800f', visibility: 'public' })
CREATE (transactions:Filed { fqn: 'bank.Bank.transactions', name: 'transactions', hash: 'ID_26f6f03c78d810161eae7a7ff5534fd588bc80af', visibility: 'public' })
CREATE (accountsBank:Field { fqn: 'bank.Bank.accounts', name: 'accounts', hash: 'ID_378ed8adfffc2de1bfd23cb72bef113d8e2ac58b', visibility: 'public' })
CREATE (privateCustomers:Field { fqn: 'bank.Bank.privateCustomers', name: 'privateCustomers', hash: 'ID_4862d1a456a6673eb3424c5f0c4248adf01a23cf', visibility: 'public' })
CREATE (creditsBank:Field { fqn: 'bank.Bank.credits', name: 'credits', hash: 'ID_3eaddf68cce19ade935a238d2ed45cf76b27416e', visibility: 'public' })
CREATE (size:Variable {name: 'size'})
CREATE (name:Variable {name: 'name'})
CREATE (position:Variable {name: 'position'})
CREATE (value:Variable {name: 'value'})
CREATE (str:Variable {name: 'str'})
CREATE (rank:Variable {name: 'rank'})
CREATE (importance:Variable {name: 'importance'})
CREATE (visibility:Variable {name: 'visibility'})
CREATE (bank)-[:CONTAINS]->(c)
CREATE (bank)-[:CONTAINS]->(p)
CREATE (bank)-[:CONTAINS]->(bk)
CREATE (p)-[:CONTAINS]->(cd)
CREATE (p)-[:CONTAINS]->(ab)
CREATE (p)-[:CONTAINS]->(tr)
CREATE (c)-[:CONTAINS]->(bc)
CREATE (c)-[:CONTAINS]->(pc)
CREATE (bc)-[:DECLARES]->(m1)
CREATE (bc)-[:DECLARES]->(m2)
CREATE (bc)-[:DECLARES]->(m3)
CREATE (bc)-[:DECLARES]->(m4)
CREATE (bc)-[:DECLARES]->(nameBusiness)
CREATE (bc)-[:DECLARES]->(accountsBusiness)
CREATE (bc)-[:DECLARES]->(creditsBusiness)
CREATE (pc)-[:DECLARES]->(m5)
CREATE (pc)-[:DECLARES]->(m6)
CREATE (pc)-[:DECLARES]->(m7)
CREATE (pc)-[:DECLARES]->(m8)
CREATE (pc)-[:DECLARES]->(m9)
CREATE (pc)-[:DECLARES]->(bankBook)
CREATE (pc)-[:DECLARES]->(accountsPrivate)
CREATE (pc)-[:DECLARES]->(creditsPrivate)
CREATE (pc)-[:DECLARES]->(namePrivate)
CREATE (cd)-[:DECLARES]->(m10)
CREATE (ac)-[:DECLARES]->(m11)
CREATE (ab)-[:DECLARES]->(m12)
CREATE (ab)-[:DECLARES]->(m13)
CREATE (ab)-[:DECLARES]->(m14)
CREATE (ab)-[:DECLARES]->(m15)
CREATE (ab)-[:DECLARES]->(m16)
CREAte (ab)-[:DECLARES]->(m17)
CREATE (ab)-[:DECLARES]->(executeTransaction)
CREATE (ab)-[:DECLARES]->(transactionIn)
CREATE (ab)-[:DECLARES]->(balance)
CREATE (ab)-[:DECLARES]->(productNumber)
CREATE (tr)-[:DECLARES]->(m18)
CREATE (tr)-[:DECLARES]->(m19)
CREATE (tr)-[:DECLARES]->(secondProduct)
CREATE (tr)-[:DECLARES]->(firstProduct)
CREATE (bb)-[:DECLARES]->(m20)
CREATE (bk)-[:DECLARES]->(m21)
CREATE (bk)-[:DECLARES]->(m22)
CREATE (bk)-[:DECLARES]->(m23)
CREATE (bk)-[:DECLARES]->(m24)
CREATE (bk)-[:DECLARES]->(m25)
CREATE (bk)-[:DECLARES]->(m26)
CREATE (bk)-[:DECLARES]->(m27)
CREATE (bk)-[:DECLARES]->(m28)
CREATE (bk)-[:DECLARES]->(bankName)
CREATE (bk)-[:DECLARES]->(bankBooks)
CREATE (bk)-[:DECLARES]->(businessCustomers)
CREATE (bk)-[:DECLARES]-> (accountsBank)
CREATE (bk)-[:DECLARES]->(transactions)
CREATE (bk)-[:DECLARES]->(privateCustomers)
CREATE (bk)-[:DECLARES]->(creditsBank)
CREATE (bk)-[:READS]->(privateAccounts)
CREATE (m1)-[:WRITES]->(nameBusiness)
CREATE (m8)-[:WRITES]->(namePrivate)
CREATE (m14)-[:WRITES]->(transactionIn)
CREATE (m22)-[:WRITES]->(bankName)
CREATE (m2)-[:READS]->(nameBusiness)
CREATE (m3)-[:READS]->(accountsBusiness)
CREATE (m4)-[:READS]->(creditsBusiness)
CREATE (m5)-[:READS]->(bankBook)
CREATE (m6)-[:READS]->(accountsPrivate)
CREATE (m7)-[:READS]->(creditsPrivate)
CREATE (m9)-[:READS]->(namePrivate)
CREATE (m12)-[:READS]->(executeTransaction)
CREATE (m13)-[:READS]->(transactionIn)
CREATE (m16)-[:READS]->(productNumber)
CREATE (m17)-[:READS]->(balance)
CREATE (m18)-[:READS]->(secondProduct)
CREATE (m19)-[:READS]->(firstProduct)

CREATE (m21)-[:READS]->(bankName)
CREATE (m22)-[:READS]->(bankBooks)
CREATE (m24)-[:READS]->(businessCustomers)
CREATE (m26)-[:READS]->(accountsBusiness) //bc
CREATE (m25)-[:READS]->(transactions)
CREATE (m26)-[:READS]->(creditsBank)
CREATE (m28)-[:READS]->(creditsBank)
CREATE (m27)-[:READS]->(privateCustomers)
CREATE (m22)-[:READS]->(secondProduct)
CREATE (m24)-[:READS]->(firstProduct)
CREATE (m25)-[:READS]->(bankBook)
CREATE (m26)-[:READS]->(namePrivate) //pc
CREATE (m26)-[:READS]->(executeTransaction) //ab
CREATE (m21)-[:READS]->(transactionIn) // ab
CREATE (m26)-[:READS]->(transactionIn) // ab
CREATE (m26)-[:READS]->(balance) // ab
CREATE (m26)-[:READS]->(productNumber) //ab

CREATE (m17)-[:DECLARES]->(size)
CREATE (m12)-[:DECLARES]->(position)
CREATE (m12)-[:DECLARES]->(name)
CREATE (m27)-[:DECLARES]->(name)
CREATE (m27)-[:DECLARES]->(position)
CREATE (m27)-[:DECLARES]->(size)
CREATE (m27)-[:DECLARES]->(value)
CREATE (m27)-[:DECLARES]->(str)
CREATE (m27)-[:DECLARES]->(rank)
CREATE (m27)-[:DECLARES]->(importance)
CREATE (m27)-[:DECLARES]->(visibility)
CREATE (m24)-[:DECLARES]->(name)
CREATE (m24)-[:DECLARES]->(position)
CREATE (m24)-[:DECLARES]->(size)
CREATE (m24)-[:DECLARES]->(value)
CREATE (m24)-[:DECLARES]->(str)
CREATE (m24)-[:DECLARES]->(rank)
CREATE (m24)-[:DECLARES]->(importance)
CREATE (m24)-[:DECLARES]->(visibility)







