# Project springboot_scaffold
Create Scaffold for springboot framework (jpa repository, spring mvc, thymeleaf)

# Process overview
Thymeleaf controller will invoke 'process' method of form instance. The form
instance will modify the model object of Thymeleaf framework for it requested
rendering. Then it returns render requested Thymeleaf view.

# Process in detail
## Toplevel 'process' method:
* Package requests into Scaffold framework model object
* Determine target usecase base on 'op' request parameters
* Invoke usecase correspondence method
## Usecase correspondence methods:
### Concepts:
* Root object:   target object will be persisted via Repository
* Child object:  property of Root object travel by subform editing
* SessionObject: For usecases that leverage subform to fullfill its operations, multiple requests states need to be persisted into an session object. It holds:
    * ICommandObject: object hold posted or rendered data of http form
        * Flat property: post data to update currently edited child object
    * Utilities: object hold utilities helper object like key converter, request utilities...
### Usecase classification:
* Usecases that operate on root object
* Usecases that operate on child object
### Usecase name convention:
* Prefix convention:
* Prefix 'show': render form method
* Prefix 'do'  : process form method, render action will be delegated to another controller via Thymeleaf redirecting mechanism
### Usecase list:
* Usecases that operate on root object
    * showRootListPage:
        * Clear session
        * Delegate repository to load list of root objects
        * Create Thymeleaf model attribute variable
        * Return related template so that Thymeleaf could render content
    * showRootCreatePage:
        * Create session object (leverage loadTargetObject)
            * Create Thymeleaf model attribute variables for session object
        * Create Thymeleaf model attribute variables: ICommandObject, Utilities
            * Compute 'actionCode' for ICommandObject
            * Compute 'actionUrls' for ICommandObject
        * Return related template so that Thymeleaf could render content
    * showRootDetailPage:
        * Create session object (leverage loadTargetObject)
            * Create Thymeleaf model attribute variables for session object
            * Compute 'actionUrls' for ICommandObject
        * Compute root key from url parameter
        * Delegate repository to load root object by computed root key
        * Update ICommandObject loaded root object
    * doRootDeletePage:
        * Compute root key from url parameter
        * Delegate repository to load root object by computed root key
        * Delegate repository to delete loaded root object
        * Prepare Thymeleaf request to redirect to showRootListPage
    * doCreateRootPage:
        * Load session object (leverage loadTargetObject)
        * Copy ICommandObject flat properties to child objects
        * Delegate repository to persit root object
        * Clear session object
        * Prepare Thymeleaf request to redirect to showRootListPage
    * doUpdateRootPage:
        * Load session object (leverage getTargetObject)
        * Compute root key from url parameter
        * Copy ICommandObject flat properties to child objects
        * Delegate repository to re-load root object by computed root key (JPA work arround: this reloads root object since transaction is expired)
        * Delegate repository to persit root object
        * Clear session object
        * Prepare Thymeleaf request to redirect to showRootListPage
* Usecases that operate on child object
    * showChildViewPage:
        * Load session object (leverage loadTargetObject)
            * Create Thymeleaf model attribute variables for session object
            * Compute 'actionUrls' for ICommandObject
        * Get target child object by id encoded in http url (leverage KeyPool of Utilities)
        * Transfer child object to flat property
        * Return related template so that Thymeleaf could render content
    * doChildCreatePage:
        * Load session object (leverage getTargetObject)
        * Transfer flat property to child object
        * Prepare Thymeleaf request to redirect to:
            * doChildCreatePage: if parent subform operates on child object
            * createRootPageShow: if parent form operates on root object
        * Note that for multiple create child object invoking, falt property does not need to be created new or refreshed because of http request nature (posted object is recreated for each request)
    * doChildDeletePage:
        * Load session object (leverage getTargetObject)
        * Get target child object by id encoded in http url (leverage KeyPool of Utilities)
        * Remove child object from root object
        * Prepare Thymeleaf request to redirect to parent form page (with related 'op' parameter)
    * T.B.D (be added more)
* Issues:
    * Version 0.0.6:
        * Helper method travelSessionObjectFromRootToParent of class CommandObjectPropertyUtils do not take child object key in to account. This results in traveling stuck at property of List collection type (we need index of the list to retrieve the child object)
