from hexdoc.patchouli.page import PageWithTitle


class ExamplePage(PageWithTitle, type="fishcasting:example"):
    """This is the Pydantic model for the `fishcasting:example` page type."""

    example_value: str
