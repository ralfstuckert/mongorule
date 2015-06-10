

package snippets.mongorule;

import org.junit.rules.ExternalResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver.IndexDefinitionHolder;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.Assert;

/**
 * This rule drops a given set of mongo collections before and after a test in
 * order to provide a proper set up. Spring automatically creates indices from
 * {@link Indexed} annotations when spring is started, but they are not recreated 
 * after dropping a collection (see <a href="https://jira.spring.io/browse/DATAMONGO-345">DATAMONGO-345</a>
 * for more details on this discussion). So in order to provide proper conditions,
 * this rule also recreate the indices after dropping the collection. <br/><br/>
 * The {@link MongoTemplate} required to perform the database operations is provided
 * by the test class either as a member variable or by a getter method. This allows you to
 * let spring inject the template as you usually do. The default name of the member variable is
 * <code>mongoTemplate</code>, and <code>getMongoTemplate</code> for the getter. You may pass
 * a different name if it doesn't fit your needs.
 */
public class MongoCleanupRule extends ExternalResource {

	private final Object testClassInstance;
	private final Class<?>[] collectionClasses;
	private final String fieldName;
	private final String getterName;

	/**
	 * Creates a MongoCleanupRule using the template names <code>mongoTemplate</code> for the member 
	 * variable, and <code>getMongoTemplate</code> for the getter.
	 * @param testClassInstance the test class instance itself.
	 * @param collectionClasses the entity classes representing the collections to clean.
	 */
	public MongoCleanupRule(final Object testClassInstance, final Class<?>... collectionClasses) {
		this(testClassInstance, "mongoTemplate", "getMongoTemplate", collectionClasses);
	}

	/**
	 * Creates a MongoCleanupRule with given name for the template member resp. getter.
	 * @param testClassInstance the test class instance itself.
	 * @param fieldOrGetterName the name of the mongo template member variable resp. getter method.
	 * @param collectionClasses the entity classes representing the collections to clean.
	 */
	public MongoCleanupRule(final Object testClassInstance, final String fieldOrGetterName,
			final Class<?>... collectionClasses) {
		this(testClassInstance, fieldOrGetterName, fieldOrGetterName, collectionClasses);
	}

	protected MongoCleanupRule(final Object testClassInstance, final String fieldName,
			final String getterName, final Class<?>... collectionClasses) {
		Assert.notNull(testClassInstance, "parameter 'testClassInstance' must not be null");
		Assert.notNull(fieldName, "parameter 'fieldName' must not be null");
		Assert.notNull(getterName, "parameter 'getterName' must not be null");
		Assert.notNull(collectionClasses, "parameter 'collectionClasses' must not be null");
		Assert.noNullElements(collectionClasses,
				"array 'collectionClasses' must not contain null elements");

		this.fieldName = fieldName;
		this.getterName = getterName;
		this.testClassInstance = testClassInstance;
		this.collectionClasses = collectionClasses;
	}

	@Override
	protected void before() throws Throwable {
		dropCollections();
		createIndeces();
	}

	@Override
	protected void after() {
		dropCollections();
	}

	protected Class<?>[] getMongoCollectionClasses() {
		return collectionClasses;
	}

	protected void dropCollections() {
		for (final Class<?> type : getMongoCollectionClasses()) {
			getMongoTemplate().dropCollection(type);
		}
	}

	protected void createIndeces() {
		for (final Class<?> type : getMongoCollectionClasses()) {
			createIndecesFor(type);
		}
	}

	protected void createIndecesFor(final Class<?> type) {
		final MongoMappingContext mappingContext =
				(MongoMappingContext) getMongoTemplate().getConverter().getMappingContext();
		final MongoPersistentEntityIndexResolver indexResolver =
				new MongoPersistentEntityIndexResolver(mappingContext);
		for (final IndexDefinitionHolder indexToCreate : indexResolver.resolveIndexForClass(type)) {
			createIndex(indexToCreate);
		}
	}

	private void createIndex(final IndexDefinitionHolder indexDefinition) {
		getMongoTemplate().getDb().getCollection(indexDefinition.getCollection())
				.createIndex(indexDefinition.getIndexKeys(), indexDefinition.getIndexOptions());
	}

	protected MongoTemplate getMongoTemplate() {
		try {
			Object value = ReflectionTestUtils.getField(testClassInstance, fieldName);
			if (value instanceof MongoTemplate) {
				return (MongoTemplate) value;
			}
			value = ReflectionTestUtils.invokeGetterMethod(testClassInstance, getterName);
			if (value instanceof MongoTemplate) {
				return (MongoTemplate) value;
			}
		} catch (final IllegalArgumentException e) {
			// throw exception with dedicated message at the end
		}
		throw new IllegalArgumentException(
				String.format(
						"%s expects either field '%s' or method '%s' in order to access the required MongoTemmplate",
						this.getClass().getSimpleName(), fieldName, getterName));
	}


}
